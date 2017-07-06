package au.com.williamhill.flywheel.edge.backplane.kafka;

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

import au.com.williamhill.flywheel.util.*;

public final class MockKafkaTest {
  private static final String TOPIC = "test";
  
  @Test
  public void test() throws InterruptedException {
    test(100, 3, 1, 5);
  }
  
  private static final class TestConsumer<K, V> extends Thread {
    private final Kafka<K, V> kafka;
    
    private final KeyedBlockingQueue<Integer, ConsumerRecord<K, V>> received = 
        new KeyedBlockingQueue<>(LinkedBlockingQueue::new);
    
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
        records.forEach(r -> received.forKey(r.partition()).add(r));
      }
      consumer.close();
    }
    
    void terminate() throws InterruptedException {
      running = false;
      interrupt();
    }
  }

  private static void test(int messages, int partitions, int sendIntervalMillis, int numConsumers) throws InterruptedException {
    final int maxHistory = messages * partitions;
    final MockKafka<Integer, Integer> kafka = new MockKafka<>(partitions, maxHistory);
    final Properties props = new Properties();
    props.put("key.serializer", IntegerSerializer.class.getName());
    props.put("value.serializer", IntegerSerializer.class.getName());
    final MockProducer<Integer, Integer> producer = kafka.getProducer(props);
    final List<TestConsumer<Integer, Integer>> consumers = new ArrayList<>(numConsumers);
    
    final AtomicInteger sent = new AtomicInteger();
    for (int m = 0; m < messages; m++) {
      for (int p = 0; p < partitions; p++) {
        producer.send(new ProducerRecord<>(TOPIC, p, m, m), (metadata, cause) -> sent.incrementAndGet());
      }
      
      if (consumers.size() < numConsumers) {
        consumers.add(new TestConsumer<>(kafka, consumers.size()));
      }
      
      if (m != messages - 1) {
        TestSupport.sleep(sendIntervalMillis);
      }
    }

    final int expectedMessages = messages * partitions;
    assertEquals(expectedMessages, sent.get());
    
    while (consumers.size() < numConsumers) {
      consumers.add(new TestConsumer<>(kafka, consumers.size()));
    }
    
    try {
      Awaitility.await().dontCatchUncaughtExceptions().atMost(10, SECONDS)
      .until(() -> consumers.stream().filter(c -> c.received.totalSize() < expectedMessages).count() == 0);
    } finally {
      for (TestConsumer<Integer, Integer> consumer : consumers) {
        assertEquals(expectedMessages, consumer.received.totalSize());
        assertEquals(partitions, consumer.received.asMap().size());
        for (Map.Entry<Integer, BlockingQueue<ConsumerRecord<Integer, Integer>>> entry : consumer.received.asMap().entrySet()) {
          final List<ConsumerRecord<Integer, Integer>> records = new ArrayList<>(entry.getValue());
          assertEquals(messages, records.size());
          for (int m = 0; m < messages; m++) {
            final ConsumerRecord<Integer, Integer> cr = records.get(m);
            assertEquals(m, (int) cr.key());
            assertEquals(m, (int) cr.value());
          }
        }
      }
    }
    
    assertTrue("history.size=" + producer.history().size(), producer.history().size() <= maxHistory);
    
    for (TestConsumer<?, ?> consumer : consumers) {
      consumer.terminate();
    }
    producer.close();
    
    for (TestConsumer<?, ?> consumer : consumers) {
      consumer.join();
    }
  }
}
