package au.com.williamhill.flywheel.logging;

import java.util.*;
import java.util.function.*;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;


/**
 *  Log4j Appender for sending events to Splunk via HEC Endpoint.<p/>
 *  
 *  Adapted from https://github.com/damiendallimore/SplunkJavaLogging.
 */
public final class SplunkHECAppender extends AppenderSkeleton {
  // connection settings
  private HECTransportConfig config = new HECTransportConfig();

  // queuing settings
  private String maxQueueSize;
  private boolean dropEventsOnQueueFull = true;

  private volatile SplunkHECInput shi;
  
  public SplunkHECAppender() {}

  public SplunkHECAppender(Layout layout) {
    this.layout = layout;
  }
  
  private static void setPropertyConditional(String key, Consumer<String> setter) {
    final String value = System.getProperty(key);
    if (value != null) setter.accept(value);
  }

  /**
   *  Log the message.
   *  
   *  @param event The log event.
   */
  @Override
  protected void append(LoggingEvent event) {
    try {
      if (shi == null) {
        setPropertyConditional("flywheel.logging.splunk.token", this::setToken);
        setPropertyConditional("flywheel.logging.splunk.url", this::setUrl);
        setPropertyConditional("flywheel.logging.splunk.index", this::setIndex);
        setPropertyConditional("flywheel.logging.splunk.source", this::setSource);
        
        synchronized (this) {
          if (shi == null) {
            shi = new SplunkHECInput(config);
            if (maxQueueSize != null) shi.setMaxQueueSize(maxQueueSize);
            shi.setDropEventsOnQueueFull(dropEventsOnQueueFull);
          }
        }
      }
    } catch (Exception e) {
      errorHandler
          .error("Couldn't establish connection for SplunkHECAppender named '"
              + this.name + "': " + Arrays.asList(e.getStackTrace()));
      e.printStackTrace(System.err);
      return;
    }

    String formatted = layout.format(event);

    // send error stack traces to splunk
    if (layout.ignoresThrowable()) {
      final String[] s = event.getThrowableStrRep();
      final StringBuilder stackTrace = new StringBuilder();
      if (s != null) {
        int len = s.length;
        for (int i = 0; i < len; i++) {
          stackTrace.append(Layout.LINE_SEP);
          stackTrace.append(s[i]);
        }
      }
      formatted += stackTrace.toString();
    }

    shi.streamEvent(formatted, event.getTimeStamp());
  }

  /**
   *  Clean up resources.
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
  
  public void setUrl(String url) {
    config.setUrl(url);
  }

  public String getToken() {
    return config.getToken();
  }

  public void setToken(String token) {
    config.setToken(token);
  }

  public int getPoolSize() {
    return config.getPoolSize();
  }

  public void setPoolSize(int poolSize) {
    config.setPoolSize(poolSize);
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

  public void setMaxInactiveTimeBeforeBatchFlush(long maxInactiveTimeBeforeBatchFlush) {
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
