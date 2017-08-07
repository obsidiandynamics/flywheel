package au.com.williamhill.flywheel.logging;

import java.net.*;
import java.util.*;

import javax.net.ssl.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.concurrent.*;
import org.apache.http.config.*;
import org.apache.http.conn.routing.*;
import org.apache.http.entity.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.impl.nio.conn.*;
import org.apache.http.impl.nio.reactor.*;
import org.apache.http.nio.conn.*;
import org.apache.http.nio.conn.ssl.*;
import org.apache.http.nio.reactor.*;
import org.apache.http.ssl.*;
import org.apache.http.util.*;

/**
 *  Common HEC logic shared by all appenders/handlers.<p/>
 *  
 *  Adapted from https://github.com/damiendallimore/SplunkJavaLogging.
 */
final class SplunkHECInput extends SplunkInput {
  private static final boolean DEBUG = false;
  
  // connection props
  private final HECTransportConfig config;

  // batch buffer
  private final List<String> batchBuffer;
  private long currentBatchSizeBytes = 0;
  private long lastEventReceivedTime;

  private final CloseableHttpAsyncClient httpClient;
  private final URI uri;
  
  private final Object lock = new Object();

  private static final HostnameVerifier HOSTNAME_VERIFIER = (s, sslSession) -> true;

  SplunkHECInput(HECTransportConfig config) throws Exception {
    this.config = config;

    this.batchBuffer = new LinkedList<>();
    this.lastEventReceivedTime = System.currentTimeMillis();

    final Registry<SchemeIOSessionStrategy> sslSessionStrategy = RegistryBuilder
        .<SchemeIOSessionStrategy>create()
        .register("http", NoopIOSessionStrategy.INSTANCE)
        .register("https",
                  new SSLIOSessionStrategy(getSSLContext(),
                                           HOSTNAME_VERIFIER)).build();

    final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
    final PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor, sslSessionStrategy);
    cm.setMaxTotal(config.getPoolSize());
    cm.setDefaultMaxPerRoute(config.getPoolSize());

    final HttpHost splunk = new HttpHost(config.getHost(), config.getPort());
    cm.setMaxPerRoute(new HttpRoute(splunk), config.getPoolSize());

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
          synchronized (lock) {
            if ((currentTime - lastEventReceivedTime) >= config
                .getMaxInactiveTimeBeforeBatchFlush()) {
              if (batchBuffer.size() > 0) {
                currentMessage = rollOutBatchBuffer();
                batchBuffer.clear();
                currentBatchSizeBytes = 0;
                hecPost(currentMessage);
              }
            }
          }

          Thread.sleep(1000);
        } catch (Exception e) {
          System.err.println("Splunk: handling batch: " + e);
          e.printStackTrace(System.err);
          
          synchronized (lock) {
            // something went wrong, put message on the queue for retry
            enqueueAndReopen(currentMessage);
          }
        }
      }
    }
  }

  private static SSLContext getSSLContext() {
    try {
      return SSLContexts.custom().loadTrustMaterial(null, (certificate, authType) -> true).build();
    } catch (Exception e) {
      System.err.println("Splunk: error constructing SSL context: " + e);
      e.printStackTrace(System.err);
      return null;
    }
  }

  private void openStream() throws Exception {
    httpClient.start();
  }

  void closeStream() {
    try {
      httpClient.close();
    } catch (Exception e) {
      System.err.println("Splunk: error closing stream: " + e);
      e.printStackTrace(System.err);
    }
  }

  private String escapeAndQuote(final String message) {
    final String trimmed = message.substring(0, message.length() - 1);
    return "\"" + trimmed.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  /**
   *  Send an event via stream.
   * 
   *  @param message The message to send.
   *  @param timestamp The time of the original event.
   */
  void streamEvent(String message, long timestamp) {
    String currentMessage = "";
    try {
      final String escaped = escapeAndQuote(message);

      // hand-building of JSON for speed
      final StringBuilder json = new StringBuilder();
      json.append("{\"")
      .append("time\":").append(timestamp).append(",\"")
      .append("event\":").append(escaped).append(",\"")
      .append("index\":\"").append(config.getIndex())
      .append("\",\"").append("source\":\"")
      .append(config.getSource()).append("\",\"")
      .append("sourcetype\":\"").append(config.getSourcetype())
      .append("\"").append("}");

      currentMessage = json.toString();

      synchronized (lock) {
        if (config.isBatchMode()) {
          lastEventReceivedTime = timestamp;
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
          final String messageOffQueue = dequeue();
          currentMessage = messageOffQueue;
          hecPost(currentMessage);
        }
      }
    } catch (Exception e) {
      System.err.println("Splunk: error streaming event: " + e);
      e.printStackTrace(System.err);
      
      synchronized (lock) {
        // something went wrong, put message on the queue for retry
        enqueueAndReopen(message);
      }
    }
  }
  
  private void enqueueAndReopen(String message) {
    enqueue(message);
    try {
      closeStream();
    } catch (Exception e) {
      System.err.println("Splunk: error closing stream: " + e);
      e.printStackTrace(System.err);
    }

    try {
      openStream();
    } catch (Exception e) {
      System.err.println("Splunk: error opening stream: " + e);
      e.printStackTrace(System.err);
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

  private void hecPost(String payload) throws Exception {
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