package au.com.williamhill.flywheel.edge.backplane.kafka;

import static org.junit.Assert.*;

import java.time.*;
import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import org.junit.*;
import org.mockito.*;
import org.slf4j.*;

import com.obsidiandynamics.socketx.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.frame.*;

public final class KafkaBackplaneTest {
  private MockKafka<String, KafkaData> kafka;
  
  private KafkaBackplaneConfig config;
  
  private Logger logger;
  
  private KafkaBackplane backplane;
  
  @Before
  public void before() {
    kafka = new MockKafka<>();
    config = createBackplaneConfig();
    logger = Mockito.mock(Logger.class);
    backplane = new KafkaBackplane(config, "test", "0", KafkaBackplane.createErrorHandler(logger));
  }
  
  private KafkaBackplaneConfig createBackplaneConfig() {
    return new KafkaBackplaneConfig() {{
      kafka = KafkaBackplaneTest.this.kafka;
      serializer = ScramjetSerializer.class;
      deserializer = ScramjetDeserializer.class;
    }};
  }
  
  @After
  public void after() throws Exception {
    backplane.close();
  }

  @Test
  public void testAttach() {
    attach();
  }
  
  private BackplaneConnector attach() {
    final BackplaneConnector connector = Mockito.mock(BackplaneConnector.class);
    backplane.attach(connector);
    return connector;
  }
  
  private Consumer<String, KafkaData> getConsumer() {
    final Properties props = new Properties();
    props.setProperty("key.deserializer", StringDeserializer.class.getName());
    props.setProperty("value.deserializer", config.deserializer.getName());
    return kafka.getConsumer(props);
  }
  
  private Producer<String, KafkaData> getProducer() {
    final Properties props = new Properties();
    props.setProperty("key.serializer", StringSerializer.class.getName());
    props.setProperty("value.serializer", config.serializer.getName());
    return kafka.getProducer(props);
  }

  @Test
  public void testOnPublishText() {
    attach();
    backplane.onPublish(new EdgeNexus(null, LocalPeer.instance()), new PublishTextFrame("topic", "hello"));
    final Consumer<String, KafkaData> consumer = getConsumer();
    consumer.subscribe(Arrays.asList(config.topic));
    final ConsumerRecords<String, KafkaData> records = consumer.poll(Duration.ofMillis(1000));
    assertNotNull(records);
    assertEquals(1, records.count());
    final ConsumerRecord<String, KafkaData> record = records.iterator().next();
    assertEquals("hello", record.value().getTextPayload());
  }

  @Test
  public void testOnPublishBinary() {
    attach();
    backplane.onPublish(new EdgeNexus(null, LocalPeer.instance()), new PublishBinaryFrame("topic", "hello".getBytes()));
    final Consumer<String, KafkaData> consumer = getConsumer();
    consumer.subscribe(Arrays.asList(config.topic));
    final ConsumerRecords<String, KafkaData> records = consumer.poll(Duration.ofMillis(1000));
    assertNotNull(records);
    assertEquals(1, records.count());
    final ConsumerRecord<String, KafkaData> record = records.iterator().next();
    assertArrayEquals("hello".getBytes(), record.value().getBinaryPayload());
  }
  
  @Test
  public void testOnReceiveText() {
    final BackplaneConnector connector = attach();
    final KafkaData d = new KafkaData("id", "source", "topic", null, "hello", 
                                      System.currentTimeMillis(), System.currentTimeMillis() + 10_000);
    getProducer().send(new ProducerRecord<>(config.topic, d));
    SocketUtils.await().until(() -> {
      Mockito.verify(connector).publish(Mockito.eq("topic"), Mockito.eq("hello"));
    });
  }
  
  @Test
  public void testOnReceiveBinary() {
    final BackplaneConnector connector = attach();
    final byte[] bytes = "hello".getBytes();
    final KafkaData d = new KafkaData("id", "source", "topic", bytes, null, 
                                      System.currentTimeMillis(), System.currentTimeMillis() + 10_000);
    getProducer().send(new ProducerRecord<>(config.topic, d));
    SocketUtils.await().until(() -> {
      Mockito.verify(connector).publish(Mockito.eq("topic"), Mockito.eq(bytes));
    });
  }
  
  @Test
  public void testOnReceiveError() {
    final BackplaneConnector connector = attach();
    final KafkaData d = new KafkaData(new RuntimeException("boom"));
    getProducer().send(new ProducerRecord<>(config.topic, d));
    SocketUtils.await().until(() -> {
      Mockito.verify(connector, Mockito.never()).publish(Mockito.anyString(), Mockito.anyString());
      Mockito.verify(connector, Mockito.never()).publish(Mockito.anyString(), Mockito.any(byte[].class));
      Mockito.verify(logger, Mockito.atLeastOnce()).warn(Mockito.any(), Mockito.any(RuntimeException.class));
    });
  }
  
  @Test
  public void testToString() {
    assertNotNull(backplane.toString());
  }
}
