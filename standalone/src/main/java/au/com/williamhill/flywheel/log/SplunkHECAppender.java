package au.com.williamhill.flywheel.log;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

/**
 * Log4j Appender for sending events to Splunk via HEC Endpoint
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class SplunkHECAppender extends AppenderSkeleton {

  // connection settings
  private HECTransportConfig config = new HECTransportConfig();

  // queuing settings
  private String maxQueueSize;
  private boolean dropEventsOnQueueFull;

  private SplunkHECInput shi;

  /**
   * Constructor
   */
  public SplunkHECAppender() {
  }

  /**
   * Constructor
   * 
   * @param layout
   *            the layout to apply to the log event
   */
  public SplunkHECAppender(Layout layout) {

    this.layout = layout;
  }

  /**
   * Log the message
   */
  @Override
  protected void append(LoggingEvent event) {

    try {
      if (shi == null) {
        shi = new SplunkHECInput(config);
        shi.setMaxQueueSize(maxQueueSize);
        shi.setDropEventsOnQueueFull(dropEventsOnQueueFull);
      }
    } catch (Exception e) {
      errorHandler
          .error("Couldn't establish connection for SplunkHECAppender named \""
              + this.name + "\".");
      return;
    }

    String formatted = layout.format(event);

    // send error stack traces to splunk
    if (layout.ignoresThrowable()) {
      String[] s = event.getThrowableStrRep();
      StringBuilder stackTrace = new StringBuilder();
      if (s != null) {
        int len = s.length;
        for (int i = 0; i < len; i++) {
          stackTrace.append(Layout.LINE_SEP);
          stackTrace.append(s[i]);
        }
      }
      formatted += stackTrace.toString();
    }

    shi.streamEvent(formatted);

  }

  /**
   * Clean up resources
   */
  @Override
  synchronized public void close() {

    closed = true;
    if (shi != null) {
      try {
        shi.closeStream();
        shi = null;
      } catch (Exception e) {
        Thread.currentThread().interrupt();
        shi = null;
      }
    }

  }

  @Override
  public boolean requiresLayout() {
    return true;
  }

  public String getToken() {
    return config.getToken();
  }

  public void setToken(String token) {
    config.setToken(token);
  }

  public String getHost() {
    return config.getHost();
  }

  public void setHost(String host) {
    config.setHost(host);
  }

  public int getPort() {
    return config.getPort();
  }

  public void setPort(int port) {
    config.setPort(port);
  }

  public boolean isHttps() {
    return config.isHttps();
  }

  public void setHttps(boolean https) {
    config.setHttps(https);
  }

  public int getPoolsize() {
    return config.getPoolsize();
  }

  public void setPoolsize(int poolsize) {
    config.setPoolsize(poolsize);
  }

  public String getIndex() {
    return config.getIndex();
  }

  public void setIndex(String index) {
    config.setIndex(index);
  }

  public String getSource() {
    return config.getSource();
  }

  public void setSource(String source) {
    config.setSource(source);
  }

  public String getSourcetype() {
    return config.getSourcetype();
  }

  public void setSourcetype(String sourcetype) {
    config.setSourcetype(sourcetype);
  }

  public String getMaxQueueSize() {
    return maxQueueSize;
  }

  public void setMaxQueueSize(String maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
  }

  public boolean isDropEventsOnQueueFull() {
    return dropEventsOnQueueFull;
  }

  public void setDropEventsOnQueueFull(boolean dropEventsOnQueueFull) {
    this.dropEventsOnQueueFull = dropEventsOnQueueFull;
  }

  public long getMaxBatchSizeEvents() {
    return config.getMaxBatchSizeEvents();
  }

  public void setMaxBatchSizeEvents(long maxBatchSizeEvents) {
    config.setMaxBatchSizeEvents(maxBatchSizeEvents);
  }

  public long getMaxInactiveTimeBeforeBatchFlush() {
    return config.getMaxInactiveTimeBeforeBatchFlush();
  }

  public void setMaxInactiveTimeBeforeBatchFlush(
      long maxInactiveTimeBeforeBatchFlush) {
    config.setMaxInactiveTimeBeforeBatchFlush(maxInactiveTimeBeforeBatchFlush);
  }

  public boolean isBatchMode() {
    return config.isBatchMode();
  }

  public void setBatchMode(boolean batchMode) {
    config.setBatchMode(batchMode);
  }

  public String getMaxBatchSizeBytes() {
    return String.valueOf(config.getMaxBatchSizeBytes());
  }

  public void setMaxBatchSizeBytes(String maxBatchSizeBytes) {
    config.setMaxBatchSizeBytes(maxBatchSizeBytes);
  }
}
