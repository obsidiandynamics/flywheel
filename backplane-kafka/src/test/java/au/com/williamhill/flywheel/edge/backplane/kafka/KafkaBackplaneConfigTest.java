package au.com.williamhill.flywheel.edge.backplane.kafka;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.apache.kafka.common.serialization.*;
import org.junit.*;

import com.obsidiandynamics.yconf.*;

public final class KafkaBackplaneConfigTest {
  @Test
  public void testLoadConfig() throws IOException {
    final KafkaBackplaneConfig config = new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(KafkaBackplaneConfigTest.class.getClassLoader().getResourceAsStream("kafka-backplane-config.yaml"))
        .map(KafkaBackplaneConfig.class);
    assertNotNull(config.toString());
    
    final KafkaCluster<String, KafkaData> kafka = (KafkaCluster<String, KafkaData>) config.kafka;
    assertNotNull(kafka.getConfig().getCommonProps());
    assertNotNull(kafka.getConfig().getProducerProps());
    assertNotNull(kafka.getConfig().getConsumerProps());
    
    final Properties consumerProps = new Properties();
    consumerProps.setProperty("key.deserializer", StringDeserializer.class.getName());
    consumerProps.setProperty("value.deserializer", config.deserializer.getName());
    kafka.getConsumer(consumerProps);
    
    final Properties producerProps = new Properties();
    producerProps.setProperty("key.serializer", StringSerializer.class.getName());
    producerProps.setProperty("value.serializer", config.serializer.getName());
    kafka.getProducer(producerProps);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testNoBoostrapServers() {
    new KafkaClusterConfig().init();
  }
}
