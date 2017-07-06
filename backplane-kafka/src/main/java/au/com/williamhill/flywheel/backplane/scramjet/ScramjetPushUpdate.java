package au.com.williamhill.flywheel.backplane.scramjet;

import com.google.gson.annotations.*;

public final class ScramjetPushUpdate implements ScramjetPayload {
  static final String JSON_TYPE_NAME = "Scramjet.Messages.Push.Update";
  
  private final String topic;
  
  @SerializedName("timetolive")
  private final int timeToLive;
  
  private final Object payload;

  public ScramjetPushUpdate(String topic, int timeToLive, Object payload) {
    this.topic = topic;
    this.timeToLive = timeToLive;
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
}
