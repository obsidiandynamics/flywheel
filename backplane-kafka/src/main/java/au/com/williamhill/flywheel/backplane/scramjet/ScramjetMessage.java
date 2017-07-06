package au.com.williamhill.flywheel.backplane.scramjet;

import java.util.*;

import com.google.gson.*;
import com.google.gson.typeadapters.*;

public final class ScramjetMessage {
  private final String id;
  
  private final String messageType;
  
  private final ScramjetPayload payload;
  
  private final String publisher;
  
  private final Date sentAt;

  public ScramjetMessage(String id, String messageType, ScramjetPayload payload, String publisher, Date sentAt) {
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

  public ScramjetPayload getPayload() {
    return payload;
  }

  public String getPublisher() {
    return publisher;
  }

  public Date getSentAt() {
    return sentAt;
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
  
  private static void registerTypes(RuntimeTypeAdapterFactory<ScramjetPayload> types) {
    types.registerSubtype(ScramjetPushUpdate.class, ScramjetPushUpdate.JSON_TYPE_NAME);
    types.registerSubtype(ScramjetObject.class, ScramjetObject.JSON_TYPE_NAME);
  }
  
  public static GsonBuilder defaultGsonBuilder() {
    final GsonBuilder builder = new GsonBuilder()
        .disableHtmlEscaping()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    final RuntimeTypeAdapterFactory<ScramjetPayload> types = RuntimeTypeAdapterFactory
        .of(ScramjetPayload.class, "$type");
    
    registerTypes(types);
    builder.registerTypeAdapterFactory(types);
    return builder;
  }
}
