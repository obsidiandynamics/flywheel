package au.com.williamhill.flywheel.edge.backplane.kafka;

import org.apache.kafka.common.serialization.*;

import com.obsidiandynamics.yconf.*;

@Y
public class KafkaBackplaneConfig {
  @YInject
  public Kafka<String, KafkaData> kafka;
  
  @YInject
  public String topic = "flywheel.backplane.v1";
  
  @YInject
  public Class<? extends Serializer<KafkaData>> serializer;
  
  @YInject
  public Class<? extends Deserializer<KafkaData>> deserializer;
  
  @YInject
  public long pollTimeoutMillis = 100;
  
  @YInject
  public long ttlMillis = 300_000;
}
