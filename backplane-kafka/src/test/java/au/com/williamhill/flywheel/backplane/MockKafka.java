package au.com.williamhill.flywheel.backplane;

import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;

public final class MockKafka<K, V> implements Kafka<K, V> {
  private final MockProducer<K, V> producer;
  
  private final MockConsumer<K, V> consumer;
  
  public MockKafka() {
    //producer = new MockProducer<>();
    producer = null; //TODO
    consumer = null; //TODO
  }

  @Override
  public Producer<K, V> getProducer(Properties props) {
    return producer;
  }

  @Override
  public Consumer<K, V> getConsumer(Properties props) {
    return consumer;
  }
}
