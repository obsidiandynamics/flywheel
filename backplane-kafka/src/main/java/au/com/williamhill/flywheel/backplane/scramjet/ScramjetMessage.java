package au.com.williamhill.flywheel.backplane.scramjet;

import java.util.*;

import com.google.gson.*;

public final class ScramjetMessage {
  static final String TYPE_ATT = "$type";
  
  private final String id;
  
  private final String messageType;
  
  private final Object payload;
  
  private final String publisher;
  
  private final Date sentAt;

  public ScramjetMessage(String id, String messageType, Object payload, String publisher, Date sentAt) {
    this.id = id;
    this.messageType = messageType;
    this.payload = payload;
    this.publisher = publisher;
    this.sentAt = sentAt;
  }

  public String getId() {
    return id;
  }

  public String getMessageType() {
    return messageType;
  }
  
  public Object getPayload() {
    return payload;
  }
  
  public String getPublisher() {
    return publisher;
  }

  public Date getSentAt() {
    return sentAt;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
    result = prime * result + ((payload == null) ? 0 : payload.hashCode());
    result = prime * result + ((publisher == null) ? 0 : publisher.hashCode());
    result = prime * result + ((sentAt == null) ? 0 : sentAt.hashCode());
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
    ScramjetMessage other = (ScramjetMessage) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (messageType == null) {
      if (other.messageType != null)
        return false;
    } else if (!messageType.equals(other.messageType))
      return false;
    if (payload == null) {
      if (other.payload != null)
        return false;
    } else if (!payload.equals(other.payload))
      return false;
    if (publisher == null) {
      if (other.publisher != null)
        return false;
    } else if (!publisher.equals(other.publisher))
      return false;
    if (sentAt == null) {
      if (other.sentAt != null)
        return false;
    } else if (!sentAt.equals(other.sentAt))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ScramjetMessage [id=" + id + ", messageType=" + messageType + ", payload=" + payload + ", publisher="
           + publisher + ", sentAt=" + sentAt + "]";
  }
  
  public static ScramjetMessage fromJson(Gson gson, String json) {
    return gson.fromJson(json, ScramjetMessage.class);
  }
  
  public String toJson(Gson gson) {
    return gson.toJson(this);
  }
  
  public static GsonBuilder defaultGsonBuilder() {
    return new GsonBuilder()
        .disableHtmlEscaping()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .registerTypeAdapter(ScramjetMessage.class, new ScramjetMessageTypeAdapter());
  }
}
