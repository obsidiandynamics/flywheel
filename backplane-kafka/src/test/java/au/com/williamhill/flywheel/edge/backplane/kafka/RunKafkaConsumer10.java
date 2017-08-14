package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.*;

public final class RunKafkaConsumer10 {
  public static void main(String[] args) {
    final Properties props = new Properties();
    props.put("bootstrap.servers", "10.134.32.26:9092");
    props.put("group.id", "sample");
    props.put("key.deserializer", StringDeserializer.class.getName());
    props.put("value.deserializer", StringDeserializer.class.getName());
    final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props); 
    consumer.subscribe(Arrays.asList("platform.push"));
    try {
      while (true) {
        System.out.println("polling...");
        final ConsumerRecords<String, String> records = consumer.poll(1000);
        System.out.println("got " + records + " records");
        for (ConsumerRecord<String, String> record : records) {
          System.out.println(record.offset() + ": " + record.value());
        }
      }
    } finally {
      consumer.close();
    }
  }
}
