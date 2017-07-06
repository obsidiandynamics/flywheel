package au.com.williamhill.flywheel.backplane.kafka;

import java.util.*;

import org.apache.kafka.common.serialization.*;

import com.google.gson.*;

import au.com.williamhill.flywheel.backplane.scramjet.*;

public final class ScramjetDeserializer implements Deserializer<KafkaData> {
  private final StringDeserializer s = new StringDeserializer();
  
  private final Gson gson = ScramjetMessage.defaultGsonBuilder().create();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    s.configure(configs, isKey);
  }

  @Override
  public KafkaData deserialize(String topic, byte[] data) {
    final String json = s.deserialize(topic, data);
    final ScramjetMessage msg = ScramjetMessage.fromJson(gson, json);
    return toKafka(msg);
  }
  
  private  KafkaData toKafka(ScramjetMessage msg) {
    final ScramjetPushUpdate update = (ScramjetPushUpdate) msg.getPayload();
    final Object payload = extractPayload(update.getPayload());
    final boolean text = payload instanceof String;
    final byte[] binaryPayload = text ? null : (byte[]) payload;
    final String textPayload = text ? (String) payload : null;
    final long timestamp = msg.getSentAt().getTime();
    final long expiry = timestamp + update.getTimeToLive() * 1000l;
    return new KafkaData(msg.getId(), msg.getPublisher(), update.getTopic(), 
                         binaryPayload, textPayload, timestamp, expiry);
  }
  
  private Object extractPayload(Object payload) {
    if (payload instanceof String) {
      return payload;
    } else if (payload instanceof Map) {
      final Map<?, ?> map = (Map<?, ?>) payload;
      if (ScramjetBase64.TYPE.equals(map.get(ScramjetMessage.TYPE_ATT))) {
        final String base64 = (String) map.get(ScramjetBase64.VALUE_ATT);
        return Base64.getDecoder().decode(base64);
      } else {
        return toJson(payload); 
      }
    } else {
      return toJson(payload);
    }
  }
  
  private String toJson(Object obj) {
    return gson.toJson(obj);
  }

  @Override
  public void close() {
    s.close();
  }
}
