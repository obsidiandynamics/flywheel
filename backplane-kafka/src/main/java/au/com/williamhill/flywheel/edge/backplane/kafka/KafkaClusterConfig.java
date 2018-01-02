package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

import com.obsidiandynamics.yconf.*;

@Y
public class KafkaClusterConfig {
  @YInject
  public PropertiesBuilder common = new PropertiesBuilder();

  @YInject
  public PropertiesBuilder producer = new PropertiesBuilder();
  
  @YInject
  public PropertiesBuilder consumer = new PropertiesBuilder();
  
  void init() {
    if (common.build().getProperty("bootstrap.servers") == null) {
      throw new IllegalArgumentException("Must specify a value for 'bootstrap.servers'");
    }
  }
  
  Properties getCommonProps() {
    return common.build();
  }

  Properties getProducerProps() {
    final Properties props = getCommonProps();
    props.putAll(producer.build());
    return props;
  }
  
  Properties getConsumerProps() {
    final Properties props = getCommonProps();
    props.putAll(consumer.build());
    return props;
  }

  @Override
  public String toString() {
    return "KafkaClusterConfig [common: " + common + ", producer: " + producer + ", consumer: " + consumer + "]";
  }
}
