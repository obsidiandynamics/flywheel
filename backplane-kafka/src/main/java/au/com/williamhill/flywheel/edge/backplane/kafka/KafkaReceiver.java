package au.com.williamhill.flywheel.edge.backplane.kafka;

import org.apache.kafka.clients.consumer.*;

public class KafkaReceiver<K, V> extends Thread implements AutoCloseable {
  @FunctionalInterface
  public interface RecordHandler<K, V> {
    void handle(ConsumerRecords<K, V> records);
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
      final ConsumerRecords<K, V> records;
      try {
        records = consumer.poll(pollTimeoutMillis);
      } catch (org.apache.kafka.common.errors.InterruptException e) {
        break;
      }
      if (! records.isEmpty()) {
        handler.handle(records);
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
