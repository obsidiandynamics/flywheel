package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

import org.apache.kafka.clients.consumer.*;

public class KafkaReceiver<K, V> extends Thread implements AutoCloseable {
  @FunctionalInterface
  public interface RecordHandler<K, V> {
    void handle(ConsumerRecords<K, V> records) throws Exception;
  }
  
  private final Consumer<K, V> consumer;
  
  private final long pollTimeoutMillis;
  
  private final RecordHandler<K, V> handler;
  
  private volatile boolean running = true;
  
  public KafkaReceiver(Consumer<K, V> consumer, long pollTimeoutMillis, String threadName, RecordHandler<K, V> handler) {
    super(threadName);
    this.consumer = consumer;
    this.pollTimeoutMillis = pollTimeoutMillis;
    this.handler = handler;
    start();
  }
  
  public Consumer<K, V> getConsumer() {
    return consumer;
  }
  
  @Override 
  public void run() {
    while (running) {
      final Map<String, ConsumerRecords<K, V>> allRecords = consumer.poll(pollTimeoutMillis);
      if (! allRecords.isEmpty()) {
        for (ConsumerRecords<K, V> records : allRecords.values()) {
          try {
            handler.handle(records);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    consumer.close();
  }
  
  @Override
  public void close() throws InterruptedException {
    running = false;
  }
  
  public void await() throws InterruptedException {
    join();
  }
}
