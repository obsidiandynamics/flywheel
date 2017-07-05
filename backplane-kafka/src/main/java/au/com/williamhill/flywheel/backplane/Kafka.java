package au.com.williamhill.flywheel.backplane;

import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;

public interface Kafka<K, V> {
  Producer<K, V> getProducer(Properties props);
  
  Consumer<K, V> getConsumer(Properties props);
}
