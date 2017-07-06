package au.com.williamhill.flywheel.backplane.kafka;

import java.util.*;

public final class KafkaData {
  private final String id;
  
  private final String source;
  
  private final String route;
  
  private final byte[] binaryPayload;
  
  private final String textPayload;
  
  private final long timestamp;
  
  private final long expiry;

  public KafkaData(String id, String source, String route, byte[] binaryPayload, String textPayload, long timestamp,
                      long expiry) {
    if (binaryPayload != null ^ textPayload != null) 
      throw new IllegalArgumentException("Exactly one of 'binaryPayload' or 'textPayload' may be assigned");
    
    this.id = id;
    this.source = source;
    this.route = route;
    this.binaryPayload = binaryPayload;
    this.textPayload = textPayload;
    this.timestamp = timestamp;
    this.expiry = expiry;
  }

  public String getId() {
    return id;
  }

  public String getSource() {
    return source;
  }

  public String getRoute() {
    return route;
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
    return "KafkaMessage [id=" + id + ", source=" + source + ", route=" + route + ", binaryPayload="
           + Arrays.toString(binaryPayload) + ", textPayload=" + textPayload + ", timestamp=" + timestamp + ", expiry="
           + expiry + "]";
  }
}
