package au.com.williamhill.flywheel.log;

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
 *  Common HEC logic shared by all appenders/handlers.<p/>
 *  
 *  Adapted from https://github.com/damiendallimore/SplunkJavaLogging.
 */
public final class SplunkHECInput extends SplunkInput {
  private static final boolean DEBUG = false;
  
  // connection props
  private final HECTransportConfig config;

  // batch buffer
  private final List<String> batchBuffer;
  private long currentBatchSizeBytes = 0;
  private long lastEventReceivedTime;

  private final CloseableHttpAsyncClient httpClient;
  private final URI uri;

  private static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
    @Override public boolean verify(String s, SSLSession sslSession) {
      return true;
    }
  };

  public SplunkHECInput(HECTransportConfig config) throws Exception {
    this.config = config;

    this.batchBuffer = Collections.synchronizedList(new LinkedList<>());
    this.lastEventReceivedTime = System.currentTimeMillis();

    final Registry<SchemeIOSessionStrategy> sslSessionStrategy = RegistryBuilder
        .<SchemeIOSessionStrategy>create()
        .register("http", NoopIOSessionStrategy.INSTANCE)
        .register("https",
                  new SSLIOSessionStrategy(getSSLContext(),
                                           HOSTNAME_VERIFIER)).build();

    final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
    final PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor, sslSessionStrategy);
    cm.setMaxTotal(config.getPoolsize());
    cm.setDefaultMaxPerRoute(config.getPoolsize());

    final HttpHost splunk = new HttpHost(config.getHost(), config.getPort());
    cm.setMaxPerRoute(new HttpRoute(splunk), config.getPoolsize());

    httpClient = HttpAsyncClients.custom().setConnectionManager(cm).build();

    uri = new URIBuilder().setScheme(config.isHttps() ? "https" : "http")
        .setHost(config.getHost()).setPort(config.getPort())
        .setPath(config.getPath()).build();

    openStream();

    if (config.isBatchMode()) {
      new BatchBufferActivityCheckerThread().start();
    }
  }

  private final class BatchBufferActivityCheckerThread extends Thread {
    BatchBufferActivityCheckerThread() {
      super(BatchBufferActivityCheckerThread.class.getSimpleName());
      setDaemon(true);
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
          System.err.println("Splunk: handling batch: " + e);
          e.printStackTrace(System.err);
          
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
      @Override public boolean isTrusted(X509Certificate[] certificate, String authType) {
        return true;
      }
    };
    SSLContext sslContext = null;
    try {
      sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
    } catch (Exception e) {
      System.err.println("Splunk: error constructing SSL context: " + e);
      e.printStackTrace(System.err);
    }
    return sslContext;
  }

  private void openStream() throws Exception {
    httpClient.start();
  }

  public void closeStream() {
    try {
      httpClient.close();
    } catch (Exception e) {
    }
  }

  private String escapeAndQuote(final String message) {
    String trimmed = message.substring(0, message.length() - 1);
    return "\"" + trimmed.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  /**
   *  Send an event via stream.
   * 
   *  @param message The message to send.
   */
  public void streamEvent(String message) {
    String currentMessage = "";
    try {
      final String escaped = escapeAndQuote(message);

      // hand-building of JSON for speed
      final StringBuilder json = new StringBuilder();
      json.append("{\"")
      .append("time\":").append(System.currentTimeMillis()).append(",\"")
      .append("event\":").append(escaped).append(",\"")
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
      System.err.println("Splunk: error streaming event: " + e);
      e.printStackTrace(System.err);
      
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
    final StringBuilder sb = new StringBuilder();
    for (String event : batchBuffer) {
      sb.append(event);
    }
    return sb.toString();
  }

  private synchronized void hecPost(String payload) throws Exception {
    final HttpPost post = new HttpPost(uri);
    post.addHeader("Authorization", "Splunk " + config.getToken());

    final StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
    if (DEBUG) System.out.println("Sending " + payload);
    post.setEntity(requestEntity);
    httpClient.execute(post, new FutureCallback<HttpResponse>() {
      @Override public void completed(HttpResponse response) {
        if (DEBUG) System.out.println("Completed " + payload);
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