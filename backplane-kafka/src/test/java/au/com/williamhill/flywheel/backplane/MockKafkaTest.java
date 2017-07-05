package au.com.williamhill.flywheel.backplane;

import static java.util.concurrent.TimeUnit.*;
import static junit.framework.TestCase.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import org.awaitility.*;
import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

public final class MockKafkaTest {
  private static final String TOPIC = "test";
  
  @Test
  public void test() throws InterruptedException {
    test(100, 1, 5);
  }
  
  private static final class TestConsumer<K, V> extends Thread {
    private final Kafka<K, V> kafka;
    
    private final List<ConsumerRecord<K, V>> received = new CopyOnWriteArrayList<>();
    
    private volatile boolean running = true;
    
    TestConsumer(Kafka<K, V> kafka, int id) {
      super("TestConsumer-" + id);
      this.kafka = kafka;
      start();
    }
    
    @Override public void run() {
      final Consumer<K, V> consumer = kafka.getConsumer(new Properties());
      consumer.subscribe(Arrays.asList(TOPIC));
      while (running) {
        final ConsumerRecords<K, V> records = consumer.poll(1);
        records.forEach(received::add);
      }
      consumer.close();
    }
    
    void terminate() throws InterruptedException {
      running = false;
      interrupt();
    }
  }

  private static void test(int messages, int sendIntervalMillis, int numConsumers) throws InterruptedException {
    final int maxPartitions = 1;
    final int maxHistory = 10;
    final MockKafka<Integer, Integer> kafka = new MockKafka<>(maxPartitions, maxHistory);
    final Properties props = new Properties();
    props.put("key.serializer", IntegerSerializer.class.getName());
    props.put("value.serializer", IntegerSerializer.class.getName());
    final MockProducer<Integer, Integer> producer = kafka.getProducer(props);
    final List<TestConsumer<Integer, Integer>> consumers = new ArrayList<>(numConsumers);
    
    final AtomicInteger sent = new AtomicInteger();
    for (int i = 0; i < messages; i++) {
      producer.send(new ProducerRecord<>(TOPIC, 0, i, i), (metadata, cause) -> sent.incrementAndGet());
      
      if (consumers.size() < numConsumers) {
        consumers.add(new TestConsumer<>(kafka, consumers.size()));
      }
      
      if (i != messages - 1) {
        TestSupport.sleep(sendIntervalMillis);
      }
    }
    
    assertEquals(messages, sent.get());
    
    while (consumers.size() < numConsumers) {
      consumers.add(new TestConsumer<>(kafka, consumers.size()));
    }
    
    Awaitility.await().dontCatchUncaughtExceptions().atMost(10, SECONDS)
    .until(() -> consumers.stream().filter(c -> c.received.size() < messages).count() == 0);
    
    assertTrue("history.size=" + producer.history().size(), producer.history().size() <= maxHistory);
    
    for (TestConsumer<?, ?> consumer : consumers) {
      consumer.terminate();
    }
    producer.close();
    
    for (TestConsumer<Integer, Integer> consumer : consumers) {
      assertEquals(messages, consumer.received.size());
      for (int i = 0; i < messages; i++) {
        final ConsumerRecord<Integer, Integer> cr = consumer.received.get(i);
        assertEquals(i, (int) cr.key());
        assertEquals(i, (int) cr.value());
      }
    }
    
    for (TestConsumer<?, ?> consumer : consumers) {
      consumer.join();
    }
  }
}
