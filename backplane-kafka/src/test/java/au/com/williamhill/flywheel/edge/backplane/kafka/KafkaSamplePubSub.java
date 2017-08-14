package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.text.*;
import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

import com.obsidiandynamics.indigo.util.*;

public final class KafkaSamplePubSub {
  private static final boolean MOCK = false;
  private static final String BROKERS = "localhost:9092";
  private static final String TOPIC = "test";
  private static final String CONSUMER_GROUP = "test";
  private static final long PUBLISH_INTERVAL = 100;
  private static final Kafka<String, String> KAFKA = MOCK ? new MockKafka<>() : new KafkaCluster<>(new KafkaClusterConfig() {{
    bootstrapServers = BROKERS;
  }});
  
  private static final class SamplePublisher extends Thread implements TestSupport {
    private static Properties getProps() {
      final Properties props = new Properties();
      props.setProperty("key.serializer", StringSerializer.class.getName());
      props.setProperty("value.serializer", StringSerializer.class.getName());
      return props;
    }
    
    private final Producer<String, String> producer;
    private volatile boolean running = true;
    
    SamplePublisher() {
      super("Kafka-SamplePublisher");
      producer = KAFKA.getProducer(getProps());
      start();
    }
    
    @Override public void run() {
      while (running) {
        send();
        if (PUBLISH_INTERVAL != 0) TestSupport.sleep(PUBLISH_INTERVAL);
      }
      producer.close();
    }
    
    private void send() {
      final long now = System.currentTimeMillis();
      final String msg = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date(now));
      final ProducerRecord<String, String> rec = new ProducerRecord<>(TOPIC, String.valueOf(now), msg);
      producer.send(rec, (metadata, exception) -> {
        log("p: tx [%s], key: %s, value: %s\n", metadata, rec.key(), rec.value());
      });
    }
    
    void close() {
      running = false;
    }
  }
  
  private static final class SampleSubscriber implements TestSupport {
    private static Properties getProps() {
      final Properties props = new Properties();
      props.setProperty("group.id", CONSUMER_GROUP);
      props.setProperty("key.deserializer", StringDeserializer.class.getName());
      props.setProperty("value.deserializer", StringDeserializer.class.getName());
      return props;
    }

    private final KafkaReceiver<String, String> receiver;
    private final Consumer<String, String> consumer;
    
    SampleSubscriber() {
      consumer = KAFKA.getConsumer(getProps());
      consumer.subscribe(TOPIC);
      receiver = new KafkaReceiver<>(consumer, 100, "Kafka-SampleSubscriber", this::receive);
    }
    
    private void receive(ConsumerRecords<String, String> records) {
      for (ConsumerRecord<String, String> record : records.records()) {
        try {
          log("c: rx [%s], key: %s, value: %s\n", formatMetadata(record.topic(), record.partition(), record.offset()), record.key(), record.value());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    
    void close() throws InterruptedException {
      receiver.close();
    }
  }
  
  private static String formatMetadata(String topic, int partition, long offset) {
    return String.format("%s-%d@%d", topic, partition, offset);
  }
  
  public static void main(String[] args) {
    final SamplePublisher pub = new SamplePublisher();
    TestSupport.sleep(500);
    final SampleSubscriber sub = new SampleSubscriber();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      TestSupport.logStatic("Shutting down\n");
      try {
        pub.close();
        sub.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }, "ShutdownHook"));
    TestSupport.sleep(Long.MAX_VALUE);
  }
}
