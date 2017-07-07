package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;

public final class KafkaCluster<K, V> implements Kafka<K, V> {
  private final KafkaClusterConfig config;
  
  public KafkaCluster(KafkaClusterConfig config) {
    config.init();
    this.config = config;
  }

  @Override
  public Producer<K, V> getProducer(Properties props) {
    final Properties combinedProps = new Properties();
    combinedProps.putAll(config.getProducerProps());
    combinedProps.putAll(props);
    return new KafkaProducer<>(combinedProps);
  }

  @Override
  public Consumer<K, V> getConsumer(Properties props) {
    final Properties combinedProps = new Properties();
    combinedProps.putAll(config.getConsumerProps());
    combinedProps.putAll(props);
    return new KafkaConsumer<>(combinedProps);
  }
}
