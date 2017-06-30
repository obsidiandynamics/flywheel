package au.com.williamhill.flywheel.log;

import java.io.*;
import java.net.*;
import java.security.cert.*;
import java.util.*;

import javax.net.ssl.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.concurrent.*;
import org.apache.http.config.*;
import org.apache.http.conn.routing.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.impl.nio.conn.*;
import org.apache.http.impl.nio.reactor.*;
import org.apache.http.nio.conn.*;
import org.apache.http.nio.conn.ssl.*;
import org.apache.http.nio.reactor.*;
import org.apache.http.util.*;

/**
 * Common HEC logic shared by all appenders/handlers
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 */
public final class SplunkHECInput extends SplunkInput {
  // connection props
  private HECTransportConfig config;

  // batch buffer
  private List<String> batchBuffer;
  private long currentBatchSizeBytes = 0;
  private long lastEventReceivedTime;

  private CloseableHttpAsyncClient httpClient;
  private URI uri;

  private static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
    public boolean verify(String s, SSLSession sslSession) {
      return true;
    }
  };

  public SplunkHECInput(HECTransportConfig config) throws Exception {
    this.config = config;

    this.batchBuffer = Collections.synchronizedList(new LinkedList<>());
    this.lastEventReceivedTime = System.currentTimeMillis();

    Registry<SchemeIOSessionStrategy> sslSessionStrategy = RegistryBuilder
        .<SchemeIOSessionStrategy>create()
        .register("http", NoopIOSessionStrategy.INSTANCE)
        .register("https",
                  new SSLIOSessionStrategy(getSSLContext(),
                                           HOSTNAME_VERIFIER)).build();

    ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
    PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(
                                                                                     ioReactor, sslSessionStrategy);
    cm.setMaxTotal(config.getPoolsize());

    HttpHost splunk = new HttpHost(config.getHost(), config.getPort());
    cm.setMaxPerRoute(new HttpRoute(splunk), config.getPoolsize());

    httpClient = HttpAsyncClients.custom().setConnectionManager(cm).build();

    uri = new URIBuilder().setScheme(config.isHttps() ? "https" : "http")
        .setHost(config.getHost()).setPort(config.getPort())
        .setPath("/services/collector").build();

    openStream();

    if (config.isBatchMode()) {
      new BatchBufferActivityCheckerThread().start();
    }
  }

  class BatchBufferActivityCheckerThread extends Thread {
    BatchBufferActivityCheckerThread() {

    }

    @Override
    public void run() {
      while (true) {
        String currentMessage = "";
        try {
          long currentTime = System.currentTimeMillis();
          if ((currentTime - lastEventReceivedTime) >= config
              .getMaxInactiveTimeBeforeBatchFlush()) {
            if (batchBuffer.size() > 0) {
              currentMessage = rollOutBatchBuffer();
              batchBuffer.clear();
              currentBatchSizeBytes = 0;
              hecPost(currentMessage);
            }
          }

          Thread.sleep(1000);
        } catch (Exception e) {
          // something went wrong, put message on the queue for retry
          enqueue(currentMessage);
          try {
            closeStream();
          } catch (Exception e1) {
          }

          try {
            openStream();
          } catch (Exception e2) {
          }
        }
      }
    }
  }

  @SuppressWarnings("deprecation")
  private SSLContext getSSLContext() {
    TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
      @Override
      public boolean isTrusted(X509Certificate[] certificate,
                               String authType) {
        return true;
      }
    };
    SSLContext sslContext = null;
    try {
      sslContext = SSLContexts.custom()
          .loadTrustMaterial(null, acceptingTrustStrategy).build();
    } catch (Exception e) {
      // Handle error
    }
    return sslContext;
  }

  /**
   * open the stream
   * 
   */
  private void openStream() throws Exception {
    httpClient.start();
  }

  /**
   * close the stream
   */
  public void closeStream() {
    try {
      httpClient.close();
    } catch (Exception e) {
    }
  }

  private String escapeAndQuote(String message) {
    return "\"" + message.replace("\"", "\\\"") + "\"";
  }

  /**
   * send an event via stream
   * 
   * @param message
   */
  public void streamEvent(String message) {
    String currentMessage = "";
    try {
      final String escaped = escapeAndQuote(message);

      // could use a JSON Object, but the JSON is so trivial, just
      // building it with a StringBuffer
      final StringBuilder json = new StringBuilder();
      json.append("{\"").append("event\":").append(escaped).append(",\"")
      .append("index\":\"").append(config.getIndex())
      .append("\",\"").append("source\":\"")
      .append(config.getSource()).append("\",\"")
      .append("sourcetype\":\"").append(config.getSourcetype())
      .append("\"").append("}");

      currentMessage = json.toString();

      if (config.isBatchMode()) {
        lastEventReceivedTime = System.currentTimeMillis();
        currentBatchSizeBytes += currentMessage.length();
        batchBuffer.add(currentMessage);
        if (flushBuffer()) {
          currentMessage = rollOutBatchBuffer();
          batchBuffer.clear();
          currentBatchSizeBytes = 0;
          hecPost(currentMessage);
        }
      } else {
        hecPost(currentMessage);
      }

      // flush the queue
      while (queueContainsEvents()) {
        String messageOffQueue = dequeue();
        currentMessage = messageOffQueue;
        hecPost(currentMessage);
      }
    } catch (Exception e) {
      // something went wrong, put message on the queue for retry
      enqueue(currentMessage);
      try {
        closeStream();
      } catch (Exception e1) {
      }

      try {
        openStream();
      } catch (Exception e2) {
      }
    }
  }

  private boolean flushBuffer() {
    return (currentBatchSizeBytes >= config.getMaxBatchSizeBytes())
        || (batchBuffer.size() >= config.getMaxBatchSizeEvents());
  }

  private String rollOutBatchBuffer() {
    final StringBuffer sb = new StringBuffer();

    for (String event : batchBuffer) {
      sb.append(event);
    }

    return sb.toString();
  }

  private void hecPost(String payload) throws Exception {
    final HttpPost post = new HttpPost(uri);
    post.addHeader("Authorization", "Splunk " + config.getToken());

    final StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
    post.setEntity(requestEntity);
    httpClient.execute(post, new FutureCallback<HttpResponse>() {
      @Override public void completed(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() >= 300) {
          System.err.println("Splunk: error sending request '" + payload + "'");
          System.err.println(response.getStatusLine());
          try {
            System.err.println(EntityUtils.toString(response.getEntity()));
          } catch (Exception e) {
            e.printStackTrace(System.err);
          }
        }
      }

      @Override public void failed(Exception ex) {
        System.err.println("Splunk: error sending request '" + payload + "'");
        ex.printStackTrace(System.err);
      }

      @Override public void cancelled() {}
    });
  }
}