package au.com.williamhill.flywheel.logging;

import java.net.*;

/**
 *  Adapted from https://github.com/damiendallimore/SplunkJavaLogging.
 */
final class HECTransportConfig {
  private String token;
  private String host = "localhost";
  private int port = 8088;
  private boolean https = false;
  private String path = "/services/collector";
  private int poolSize = 1;

  private String index = "main";
  private String source = "splunk_javalogging_hec";
  private String sourcetype = "splunk_javalogging_hec";

  // data size multipliers
  private static final int KB = 1024;
  private static final int MB = KB * 1024;
  private static final int GB = MB * 1024;

  private boolean batchMode = false;
  private long maxBatchSizeBytes = 1 * MB;
  private long maxBatchSizeEvents = 100;
  private long maxInactiveTimeBeforeBatchFlush = 5000;
  
  void setUrl(String url) {
    final URL u;
    try {
      u = new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Malformed URL " + url, e);
    }
    setHttps(u.getProtocol().equals("https"));
    setHost(u.getHost());
    setPort(u.getPort());
    setPath(u.getPath());
  }

  String getToken() {
    return token;
  }

  void setToken(String token) {
    this.token = token;
  }

  String getHost() {
    return host;
  }

  void setHost(String host) {
    this.host = host;
  }

  int getPort() {
    return port;
  }

  void setPort(int port) {
    this.port = port;
  }

  boolean isHttps() {
    return https;
  }

  void setHttps(boolean https) {
    this.https = https;
  }
  
  String getPath() {
    return path;
  }

  void setPath(String path) {
    this.path = path;
  }

  int getPoolSize() {
    return poolSize;
  }

  void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
  }

  String getIndex() {
    return index;
  }

  void setIndex(String index) {
    this.index = index;
  }

  String getSource() {
    return source;
  }

  void setSource(String source) {
    this.source = source;
  }

  String getSourcetype() {
    return sourcetype;
  }

  void setSourcetype(String sourcetype) {
    this.sourcetype = sourcetype;
  }

  boolean isBatchMode() {
    return batchMode;
  }

  void setBatchMode(boolean batchMode) {
    this.batchMode = batchMode;
  }

  long getMaxBatchSizeBytes() {
    return maxBatchSizeBytes;
  }

  void setMaxBatchSizeBytes(long maxBatchSizeBytes) {
    this.maxBatchSizeBytes = maxBatchSizeBytes;
  }

  /**
   *  Set the batch size from the configured property String value. If parsing
   *  fails , the default of 500KB will be used.
   * 
   *  @param rawProperty In format [<integer>|<integer>[KB|MB|GB]].
   */
  void setMaxBatchSizeBytes(String rawProperty) {
    int multiplier;
    int factor;

    if (rawProperty.endsWith("KB")) {
      multiplier = KB;
    } else if (rawProperty.endsWith("MB")) {
      multiplier = MB;
    } else if (rawProperty.endsWith("GB")) {
      multiplier = GB;
    } else {
      return;
    }
    try {
      factor = Integer.parseInt(rawProperty.substring(0, rawProperty.length() - 2));
    } catch (NumberFormatException e) {
      return;
    }
    setMaxBatchSizeBytes(factor * multiplier);
  }

  long getMaxBatchSizeEvents() {
    return maxBatchSizeEvents;
  }

  void setMaxBatchSizeEvents(long maxBatchSizeEvents) {
    this.maxBatchSizeEvents = maxBatchSizeEvents;
  }

  long getMaxInactiveTimeBeforeBatchFlush() {
    return maxInactiveTimeBeforeBatchFlush;
  }

  void setMaxInactiveTimeBeforeBatchFlush(long maxInactiveTimeBeforeBatchFlush) {
    this.maxInactiveTimeBeforeBatchFlush = maxInactiveTimeBeforeBatchFlush;
  }
}
