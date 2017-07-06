package au.com.williamhill.flywheel.backplane;

import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;

public final class KafkaCluster<K, V> implements Kafka<K, V> {
  @Override
  public Producer<K, V> getProducer(Properties props) {
    return new KafkaProducer<>(props);
  }

  @Override
  public Consumer<K, V> getConsumer(Properties props) {
    return new KafkaConsumer<>(props);
  }
}
