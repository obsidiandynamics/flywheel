package au.com.williamhill.flywheel.backplane.scramjet;

import java.lang.reflect.*;
import java.util.*;

import com.google.gson.*;

final class ScramjetMessageTypeAdapter implements JsonSerializer<ScramjetMessage>, JsonDeserializer<ScramjetMessage> {
  @Override
  public JsonElement serialize(ScramjetMessage src, Type typeOfSrc, JsonSerializationContext context) {
    final Object packed = pack(src.getPayload());
    final JsonObject obj = new JsonObject();
    obj.add("id", context.serialize(src.getId()));
    obj.add("messageType", context.serialize(src.getMessageType()));
    obj.add("payload", context.serialize(packed));
    obj.add("publisher", context.serialize(src.getPublisher()));
    obj.add("sentAt", context.serialize(src.getSentAt()));
    return obj;
  }
  
  private static Object pack(Object payload) {
    return payload instanceof ScramjetPayload ? ((ScramjetPayload) payload).pack() : payload;
  }
  
  @Override
  public ScramjetMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    final JsonObject jsonObj = json.getAsJsonObject();
    final String id = context.deserialize(jsonObj.get("id"), String.class);
    final String messageType = context.deserialize(jsonObj.get("messageType"), String.class);
    final Object payload = context.deserialize(jsonObj.get("payload"), Object.class);
    final String publisher = context.deserialize(jsonObj.get("publisher"), String.class);
    final Date sentAt = context.deserialize(jsonObj.get("sentAt"), Date.class);
    
    final Object unpacked = payload instanceof Map ? unpack(payload) : payload;
    return new ScramjetMessage(id, messageType, unpacked, publisher, sentAt);
  }
  
  private static Object unpack(Object payload) {
    @SuppressWarnings("unchecked")
    final AttributeReader reader = new AttributeReader((Map<String, Object>) payload);
    final String typeName = reader.getTypeName();
    if (typeName != null) {
      switch (typeName) {
        case ScramjetBase64.TYPE:
          return ScramjetBase64.unpack(reader);
        
        case ScramjetPushUpdate.TYPE:
          return ScramjetPushUpdate.unpack(reader);
          
        case ScramjetObject.TYPE:
        default:
          return ScramjetObject.unpack(reader);
      }
    } else {
      return payload;
    }
  }
}