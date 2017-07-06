package au.com.williamhill.flywheel.edge.backplane.scramjet;

import java.lang.reflect.*;

import com.google.gson.*;

final class ScramjetBase64TypeAdapter implements JsonSerializer<ScramjetBase64> {
  @Override
  public JsonElement serialize(ScramjetBase64 src, Type typeOfSrc, JsonSerializationContext context) {
    final JsonObject obj = new JsonObject();
    obj.add(ScramjetMessage.TYPE_ATT, new JsonPrimitive(ScramjetBase64.TYPE));
    obj.add(ScramjetBase64.VALUE_ATT, new JsonPrimitive(src.getValue()));
    return obj;
  }
}