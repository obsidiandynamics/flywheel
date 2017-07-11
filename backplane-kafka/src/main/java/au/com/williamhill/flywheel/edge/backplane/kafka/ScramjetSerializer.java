package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

import org.apache.kafka.common.serialization.*;

import com.google.gson.*;

import au.com.williamhill.flywheel.edge.backplane.scramjet.*;

public final class ScramjetSerializer implements Serializer<KafkaData> {
  private final StringSerializer s = new StringSerializer();
  private final Gson gson = ScramjetMessage.defaultGsonBuilder().create();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    s.configure(configs, isKey);
  }
  
  @Override
  public byte[] serialize(String topic, KafkaData data) {
    final ScramjetMessage msg = toScramjet(data);
    final String json = msg.toJson(gson);
    return s.serialize(topic, json);
  }
  
  private static ScramjetMessage toScramjet(KafkaData data) {
    final int ttl = (int) (data.getTimeRemaining() / 1000);
    final int cappedTtl = ttl < 0 ? Integer.MAX_VALUE : ttl;
    final Object payload = data.isText() ? data.getTextPayload() : new ScramjetBase64(data.getBinaryPayload());
    final ScramjetPushUpdate update = new ScramjetPushUpdate(data.getRoute(), cappedTtl, payload);
    final ScramjetMessage msg = new ScramjetMessage(data.getId(), "PUSH_UPDATE", update, 
                                                    data.getSource(), new Date(data.getTimestamp()));
    return msg;
  }

  @Override
  public void close() {
    s.close(); 
  }
}
