package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.*;
import org.apache.kafka.common.serialization.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.edge.backplane.kafka.KafkaReceiver.*;
import au.com.williamhill.flywheel.frame.*;

@Y
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

  public KafkaBackplane(@YInject(name="backplaneConfig") KafkaBackplaneConfig config, 
                        @YInject(name="clusterId") String clusterId, 
                        @YInject(name="brokerId") String brokerId) {
    this.config = config;
    this.clusterId = clusterId;
    this.brokerId = brokerId;
    source = clusterId + "-" + brokerId;
  }
  
  private Properties getConsumerProps() {
    final Properties props = new Properties();
    props.setProperty("group.id", source);
    props.setProperty("enable.auto.commit", String.valueOf(false));
    props.setProperty("key.deserializer", StringDeserializer.class.getName());
    props.setProperty("value.deserializer", config.deserializer.getName());
    return props;
  }
  
  private Properties getProducerProps() {
    final Properties props = new Properties();
    props.setProperty("key.serializer", StringSerializer.class.getName());
    props.setProperty("value.serializer", config.serializer.getName());
    return props;
  }

  @Override
  public void attach(BackplaneConnector connector) {
    this.connector = connector;
    final Consumer<String, KafkaData> consumer = config.kafka.getConsumer(getConsumerProps());
    seekToEnd(consumer, config.topic);
    final String threadName = "KafkaReceiver-" + clusterId + "-" + brokerId + "-" + config.topic;
    receiver = new KafkaReceiver<>(
        consumer,
        config.pollTimeoutMillis,
        threadName,
        this);
    producer = config.kafka.getProducer(getProducerProps());
  }
  
  private static void seekToEnd(Consumer<?, ?> consumer, String topic) {
    final List<PartitionInfo> infos = consumer.partitionsFor(topic);
    if (infos != null) {
      final List<TopicPartition> partitions = infos.stream()
          .map(i -> new TopicPartition(i.topic(), i.partition())).collect(Collectors.toList());
      final Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
      consumer.assign(partitions);
      for (Map.Entry<TopicPartition, Long> entry : endOffsets.entrySet()) {
        consumer.seek(entry.getKey(), entry.getValue());
      }
    } else {
      consumer.subscribe(Arrays.asList(topic));
    }
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
    if (receiver != null) {
      receiver.close();
      receiver.await();
      receiver = null;
    }
    if (producer != null) {
      producer.close();
      producer = null;
    }
  }
}
