package au.com.williamhill.flywheel.edge.backplane.scramjet;

public final class ScramjetPushUpdate implements ScramjetPayload {
  public static final String TYPE = "Scramjet.Messages.Push.Update";
  
  private final String topic;
  
  private final int timeToLive;
  
  private final Object payload;

  public ScramjetPushUpdate(String topic, int timeToLiveSeconds, Object payload) {
    this.topic = topic;
    this.timeToLive = timeToLiveSeconds;
    this.payload = payload;
  }

  public String getTopic() {
    return topic;
  }

  public int getTimeToLive() {
    return timeToLive;
  }

  public Object getPayload() {
    return payload;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((payload == null) ? 0 : payload.hashCode());
    result = prime * result + timeToLive;
    result = prime * result + ((topic == null) ? 0 : topic.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ScramjetPushUpdate other = (ScramjetPushUpdate) obj;
    if (payload == null) {
      if (other.payload != null)
        return false;
    } else if (!payload.equals(other.payload))
      return false;
    if (timeToLive != other.timeToLive)
      return false;
    if (topic == null) {
      if (other.topic != null)
        return false;
    } else if (!topic.equals(other.topic))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ScramjetPushUpdate [topic=" + topic + ", timeToLive=" + timeToLive + ", payload=" + payload + "]";
  }

  @Override
  public AttributeWriter pack() {
    return new AttributeWriter(TYPE).write("topic", topic).write("timeToLive", timeToLive).write("payload", payload);
  }
  
  static ScramjetPushUpdate unpack(AttributeReader reader) {
    final ScramjetPushUpdate obj = new ScramjetPushUpdate(reader.read("topic"),
                                                          reader.<Number>read("timeToLive").intValue(),
                                                          reader.read("payload"));
    return obj;
  }
}
