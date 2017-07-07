package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.text.*;
import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

import com.obsidiandynamics.indigo.util.*;

public final class KafkaSamplePubSub {
  private static final boolean MOCK = false;
  private static final Kafka<String, String> KAFKA = MOCK ? new MockKafka<>() : new KafkaCluster<>();
  private static final String BROKERS = "localhost:9092";
  private static final String TOPIC = "test";
  private static final String CONSUMER_GROUP = "test";
  private static final long PUBLISH_INTERVAL = 100;
  
  private static Properties getCommonProps() {
    final Properties props = new Properties();
    props.setProperty("bootstrap.servers", BROKERS);
    return props;
  }
  
  private static final class SamplePublisher extends Thread implements TestSupport {
    private static Properties getProps() {
      final Properties props = getCommonProps();
      props.setProperty("acks", "all");
      props.setProperty("retries", String.valueOf(0));
      props.setProperty("batch.size", String.valueOf(16_384));
      props.setProperty("linger.ms", String.valueOf(1));
      props.setProperty("buffer.memory", String.valueOf(33_554_432));
      props.setProperty("key.serializer", StringSerializer.class.getName());
      props.setProperty("value.serializer", StringSerializer.class.getName());
      return props;
    }
    
    private final Producer<String, String> producer;
    
    SamplePublisher() {
      super("Kafka-SamplePublisher");
      producer = KAFKA.getProducer(getProps());
    }
    
    @Override public void run() {
      for (;;) {
        send();
        TestSupport.sleep(PUBLISH_INTERVAL);
      }
    }
    
    private void send() {
      final long now = System.currentTimeMillis();
      final String msg = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date(now));
      final ProducerRecord<String, String> rec = new ProducerRecord<>(TOPIC, String.valueOf(now), msg);
      producer.send(rec, (metadata, exception) -> {
        log("p: tx [%s], key: %s, value: %s\n", metadata, rec.key(), rec.value());
      });
    }
  }
  
  private static final class SampleSubscriber extends Thread implements TestSupport {
    private static Properties getProps() {
      final Properties props = getCommonProps();
      props.setProperty("group.id", CONSUMER_GROUP);
      props.setProperty("enable.auto.commit", String.valueOf(true));
      props.setProperty("auto.commit.interval.ms", String.valueOf(100));
      props.setProperty("key.deserializer", StringDeserializer.class.getName());
      props.setProperty("value.deserializer", StringDeserializer.class.getName());
      return props;
    }
    
    private final Consumer<String, String> consumer;
    
    SampleSubscriber() {
      super("Kafka-SampleSubscriber");
      consumer = KAFKA.getConsumer(getProps());
      consumer.subscribe(Arrays.asList(TOPIC));
    }
    
    @Override public void run() {
      for (;;) {
        receive();
      }
    }
    
    private void receive() {
      final ConsumerRecords<String, String> records = consumer.poll(100);
      for (ConsumerRecord<String, String> record : records) {
        log("c: rx [%s], key: %s, value: %s\n", formatMetadata(record.topic(), record.partition(), record.offset()), record.key(), record.value());
      }
    }
  }
  
  private static String formatMetadata(String topic, int partition, long offset) {
    return String.format("%s-%d@%d", topic, partition, offset);
  }
  
  public static void main(String[] args) {
    new SamplePublisher().start();
    TestSupport.sleep(500);
    new SampleSubscriber().start();
    TestSupport.sleep(Long.MAX_VALUE);
  }
}
