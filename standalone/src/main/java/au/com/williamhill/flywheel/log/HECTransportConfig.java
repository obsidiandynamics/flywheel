package au.com.williamhill.flywheel.log;

/**
 *  Adapted from https://github.com/damiendallimore/SplunkJavaLogging.
 */
public final class HECTransportConfig {
  private String token;
  private String host = "localhost";
  private int port = 8088;
  private boolean https = false;
  private String path = "/services/collector";
  private int poolsize = 1;

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

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isHttps() {
    return https;
  }

  public void setHttps(boolean https) {
    this.https = https;
  }
  
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getPoolsize() {
    return poolsize;
  }

  public void setPoolsize(int poolsize) {
    this.poolsize = poolsize;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSourcetype() {
    return sourcetype;
  }

  public void setSourcetype(String sourcetype) {
    this.sourcetype = sourcetype;
  }

  public boolean isBatchMode() {
    return batchMode;
  }

  public void setBatchMode(boolean batchMode) {
    this.batchMode = batchMode;
  }

  public long getMaxBatchSizeBytes() {
    return maxBatchSizeBytes;
  }

  public void setMaxBatchSizeBytes(long maxBatchSizeBytes) {
    this.maxBatchSizeBytes = maxBatchSizeBytes;
  }

  /**
   *  Set the batch size from the configured property String value. If parsing
   *  fails , the default of 500KB will be used.
   * 
   *  @param rawProperty In format [<integer>|<integer>[KB|MB|GB]].
   */
  public void setMaxBatchSizeBytes(String rawProperty) {
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

  public long getMaxBatchSizeEvents() {
    return maxBatchSizeEvents;
  }

  public void setMaxBatchSizeEvents(long maxBatchSizeEvents) {
    this.maxBatchSizeEvents = maxBatchSizeEvents;
  }

  public long getMaxInactiveTimeBeforeBatchFlush() {
    return maxInactiveTimeBeforeBatchFlush;
  }

  public void setMaxInactiveTimeBeforeBatchFlush(long maxInactiveTimeBeforeBatchFlush) {
    this.maxInactiveTimeBeforeBatchFlush = maxInactiveTimeBeforeBatchFlush;
  }
}
