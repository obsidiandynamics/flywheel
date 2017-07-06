package au.com.williamhill.flywheel.edge.backplane.kafka;

import org.apache.kafka.common.serialization.*;

public class KafkaBackplaneConfig {
  public Kafka<String, KafkaData> kafka;
  
  public String topic = "flywheel.kafka.v1";
  
  public Class<Serializer<KafkaData>> serializerClass;
  
  public Class<Deserializer<KafkaData>> deserializerClass;
  
  public long pollTimeoutMillis = 100;
  
  public long ttlMillis = 300_000;
}
