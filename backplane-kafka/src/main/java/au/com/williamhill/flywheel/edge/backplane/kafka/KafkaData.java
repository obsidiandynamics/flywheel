package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

public final class KafkaData {
  private final String id;
  
  private final String source;
  
  private final String topic;
  
  private final byte[] binaryPayload;
  
  private final String textPayload;
  
  private final long timestamp;
  
  private final long expiry;
  
  private final Throwable error;
  
  public KafkaData(Throwable error) {
    id = null;
    source = null;
    topic = null;
    binaryPayload = null;
    textPayload = null;
    timestamp = 0;
    expiry = 0;
    this.error = error;
  }

  public KafkaData(String id, String source, String topic, byte[] binaryPayload, String textPayload, 
                   long timestamp, long expiry) {
    if (! (binaryPayload != null ^ textPayload != null))
      throw new IllegalArgumentException("Exactly one of 'binaryPayload' or 'textPayload' may be assigned");
    
    this.id = id;
    this.source = source;
    this.topic = topic;
    this.binaryPayload = binaryPayload;
    this.textPayload = textPayload;
    this.timestamp = timestamp;
    this.expiry = expiry;
    error = null;
  }
  
  public boolean isError() {
    return error != null;
  }
  
  public Throwable getError() {
    return error;
  }

  public String getId() {
    return id;
  }

  public String getSource() {
    return source;
  }

  public String getTopic() {
    return topic;
  }
  
  public boolean isText() {
    return textPayload != null;
  }

  public byte[] getBinaryPayload() {
    return binaryPayload;
  }

  public String getTextPayload() {
    return textPayload;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getExpiry() {
    return expiry;
  }
  
  public long getTimeRemaining() {
    return expiry - timestamp;
  }

  @Override
  public String toString() {
    return "KafkaData [id=" + id + ", source=" + source + ", topic=" + topic + ", binaryPayload="
           + Arrays.toString(binaryPayload) + ", textPayload=" + textPayload + ", timestamp=" + timestamp + ", expiry="
           + expiry + "]";
  }
}
