package au.com.williamhill.flywheel.backplane;

import java.util.*;
import java.util.concurrent.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.*;

import com.obsidiandynamics.indigo.util.*;

public final class MockKafka<K, V> implements Kafka<K, V>, TestSupport {
  private final int maxPartitions;
  
  private final int maxHistory;
  
  private MockProducer<K, V> producer;
  
  private final List<MockConsumer<K, V>> consumers = new ArrayList<>();
  
  private List<ConsumerRecord<K, V>> backlog = new ArrayList<>();
  
  private final Object lock = new Object();
  
  public MockKafka() {
    this(10, 100);
  }
  
  public MockKafka(int maxPartitions, int maxHistory) {
    this.maxPartitions = maxPartitions;
    this.maxHistory = maxHistory;
  }
  
  @Override
  public MockProducer<K, V> getProducer(Properties props) {
    synchronized (lock) {
      if (producer == null) {
        final String keySerializer = props.getProperty("key.serializer");
        final String valueSerializer = props.getProperty("value.serializer");
        producer = new MockProducer<K, V>(true, instantiate(keySerializer), instantiate(valueSerializer)) {
          @Override public synchronized Future<RecordMetadata> send(ProducerRecord<K, V> r, Callback callback) {
            log("MockKafka: sending %s\n", r);
            final Future<RecordMetadata> f = super.send(r, new Callback() {
              @Override public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (metadata.partition() >= maxPartitions) {
                  final IllegalStateException e = new IllegalStateException(String.format("Cannot send message on partition %d, "
                      + "a maximum of %d partitions are supported", metadata.partition(), maxPartitions));
                  if (callback != null) callback.onCompletion(metadata, e);
                  throw e;
                }
                
                if (callback != null) callback.onCompletion(metadata, exception);
  
                final ConsumerRecord<K, V> cr = 
                    new ConsumerRecord<>(r.topic(), metadata.partition(), metadata.offset(), r.key(), r.value());
                
                final TopicPartition part = new TopicPartition(r.topic(), metadata.partition());
                synchronized (lock) {
                  backlog.add(cr);
                  for (MockConsumer<K, V> consumer : consumers) {
                    if (consumer.assignment().contains(part)) {
                      consumer.addRecord(cr);
                    }
                  }
                  
                  if (history().size() > maxHistory) {
                    clear();
                    pruneBacklog();
                  }
                }
              }
            });
            return f;
          }
        };
      }
    }
    return producer;
  }
  
  private void pruneBacklog() {
    if (backlog.size() > maxHistory) {
      backlog = backlog.subList(backlog.size() - maxHistory, backlog.size());
    }
  }

  @Override
  public MockConsumer<K, V> getConsumer(Properties props) {
    final MockConsumer<K, V> consumer = new MockConsumer<K, V>(OffsetResetStrategy.EARLIEST) {
      @Override public void subscribe(Collection<String> topics) {
        for (String topic : topics) {
          log("MockConsumer: assigning %s\n", topic);
          synchronized (lock) {
            final List<TopicPartition> partitions = new ArrayList<>(maxPartitions);
            final Map<TopicPartition, Long> offsetRecords = new HashMap<>();
            final List<ConsumerRecord<K, V>> records = new ArrayList<>();
            
            for (int partIdx = 0; partIdx < maxPartitions; partIdx++) {
              final TopicPartition part = new TopicPartition(topic, partIdx);
              if (! assignment().contains(part)) {
                partitions.add(part);
                offsetRecords.put(part, 0L);
                
                for (ConsumerRecord<K, V> cr : backlog) {
                  if (cr.topic().equals(topic) && cr.partition() == partIdx) {
                    records.add(cr);
                  }
                }
              }
            }

            assign(partitions);
            updateBeginningOffsets(offsetRecords);
            for (ConsumerRecord<K, V> cr : records) {
              addRecord(cr);
            }
          }
        }
      }
    };
    synchronized (lock) {
      consumers.add(consumer);
    }
    return consumer;
  }
  
  @SuppressWarnings("unchecked")
  private static <T> T instantiate(String className) {
    try {
      final Class<?> cls = Class.forName(className);
      return (T) cls.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
