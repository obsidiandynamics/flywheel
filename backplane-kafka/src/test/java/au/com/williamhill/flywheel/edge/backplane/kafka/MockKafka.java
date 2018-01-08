package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

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
    this(10, 100_000);
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
          @Override public Future<RecordMetadata> send(ProducerRecord<K, V> r, Callback callback) {
            final Future<RecordMetadata> f = super.send(r, (metadata, exception) -> {
              if (callback != null) callback.onCompletion(metadata, exception);
              final int partition = r.partition() != null ? r.partition() : metadata.partition();
              enqueue(r, partition, metadata.offset());
            });
            return f;
          }
          
          final AtomicBoolean closed = new AtomicBoolean();
          @Override public void close(long timeout, TimeUnit timeUnit) {
            if (closed.compareAndSet(false, true)) {
              super.close();
            }
          }
        };
      }
    }
    return producer;
  }
  
  private void enqueue(ProducerRecord<K, V> r, int partition, long offset) {
    if (partition >= maxPartitions) {
      final IllegalStateException e = new IllegalStateException(String.format("Cannot send message on partition %d, "
          + "a maximum of %d partitions are supported", partition, maxPartitions));
      throw e;
    }
    
    final ConsumerRecord<K, V> cr = 
        new ConsumerRecord<>(r.topic(), partition, offset, r.key(), r.value());
    
    final TopicPartition part = new TopicPartition(r.topic(), partition);
    synchronized (lock) {
      backlog.add(cr);
      for (MockConsumer<K, V> consumer : consumers) {
        if (! consumer.closed()) {
          if (consumer.assignment().contains(part)) {
            consumer.addRecord(cr);
          }
        }
      }
      
      if (producer.history().size() > maxHistory) {
        producer.clear();
        pruneBacklog();
      }
    }
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
      
      @Override
      public List<PartitionInfo> partitionsFor(String topic) {
        final List<PartitionInfo> superInfos = super.partitionsFor(topic);
        if (superInfos != null) {
          return superInfos;
        } else {
          final List<PartitionInfo> newInfos = new ArrayList<>(maxPartitions);
          final Map<TopicPartition, Long> offsets = new HashMap<>(maxPartitions);
          
          for (int i = 0; i < maxPartitions; i++) {
            newInfos.add(new PartitionInfo(topic, i, null, new Node[0], new Node[0]));
            offsets.put(new TopicPartition(topic, i), 0L);
          }
          synchronized (lock) {
            updateBeginningOffsets(offsets);
            updateEndOffsets(offsets);
          }
          return newInfos;
        }
      }
      
      @Override
      public ConsumerRecords<K, V> poll(long timeout) {
        final long endTime = System.currentTimeMillis() + timeout;
        for (;;) {
          final ConsumerRecords<K, V> recs = super.poll(timeout);
          if (! recs.isEmpty()) {
            return recs;
          } else {
            final long remainingMillis = endTime - System.currentTimeMillis();
            if (remainingMillis <= 0) {
              return recs;
            } else {
              try {
                Thread.sleep(Math.min(10, remainingMillis));
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return recs;
              }
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
      return (T) cls.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return "MockKafka [maxPartitions: " + maxPartitions + ", maxHistory: " + maxHistory + "]";
  }
}
