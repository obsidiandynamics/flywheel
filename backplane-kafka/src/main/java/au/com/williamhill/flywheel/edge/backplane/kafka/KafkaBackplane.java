package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.edge.backplane.kafka.KafkaReceiver.*;
import au.com.williamhill.flywheel.frame.*;

public final class KafkaBackplane implements Backplane, RecordHandler<String, KafkaData> {
  private final KafkaBackplaneConfig config;
  
  private final String clusterId;
  
  private final String brokerId;
  
  private final String source;
  
  private final AtomicLong idGen = new AtomicLong(randomNonNegative());
  
  private volatile BackplaneConnector connector;
  
  private volatile KafkaReceiver<String, KafkaData> receiver;
  
  private volatile Producer<String, KafkaData> producer;
  
  private static long randomNonNegative() {
    final long r = Crypto.machineRandom();
    return r < 0 ? r - Long.MIN_VALUE : r;
  }

  public KafkaBackplane(KafkaBackplaneConfig config, String clusterId, String brokerId) {
    this.config = config;
    this.clusterId = clusterId;
    this.brokerId = brokerId;
    source = clusterId + "-" + brokerId;
  }
  
  private Properties getConsumerProps() {
    final Properties props = new Properties();
    props.setProperty("key.deserializer", StringDeserializer.class.getName());
    props.setProperty("value.deserializer", config.deserializerClass.getName());
    return props;
  }
  
  private Properties getProducerProps() {
    final Properties props = new Properties();
    props.setProperty("key.serializer", StringSerializer.class.getName());
    props.setProperty("value.serializer", config.serializerClass.getName());
    return props;
  }

  @Override
  public void attach(BackplaneConnector connector) {
    this.connector = connector;
    final Consumer<String, KafkaData> consumer = config.kafka.getConsumer(getConsumerProps());
    consumer.subscribe(Arrays.asList(config.topic));
    final String threadName = "KafkaReceiver-" + clusterId + "-" + brokerId + "-" + config.topic;
    receiver = new KafkaReceiver<>(
        consumer,
        config.pollTimeoutMillis,
        threadName,
        this);
    producer = config.kafka.getProducer(getProducerProps());
  }

  @Override
  public void handle(ConsumerRecords<String, KafkaData> records) {
    for (ConsumerRecord<String, KafkaData> rec : records) {
      final KafkaData data = rec.value();
      if (! data.getSource().equals(source)) {
        if (data.isText()) {
          connector.publish(data.getRoute(), data.getTextPayload());
        } else {
          connector.publish(data.getRoute(), data.getBinaryPayload());
        }
      }
    }
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
    final long now = System.currentTimeMillis();
    final KafkaData data = new KafkaData(String.valueOf(idGen.getAndIncrement()),
                                         source,
                                         pub.getTopic(),
                                         null,
                                         pub.getPayload(),
                                         now,
                                         now + config.ttlMillis);
    producer.send(new ProducerRecord<>(config.topic, pub.getTopic(), data));
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    final long now = System.currentTimeMillis();
    final KafkaData data = new KafkaData(String.valueOf(idGen.getAndIncrement()),
                                         source,
                                         pub.getTopic(),
                                         pub.getPayload(),
                                         null,
                                         now,
                                         now + config.ttlMillis);
    producer.send(new ProducerRecord<>(config.topic, pub.getTopic(), data));
  }

  @Override
  public void close() throws Exception {
    if (receiver == null) return;
    receiver.close();
    receiver.await();
  }
}
