package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.time.*;

import org.apache.kafka.clients.consumer.*;
import org.slf4j.*;

public final class KafkaReceiver<K, V> extends Thread implements AutoCloseable {
  @FunctionalInterface
  public interface RecordHandler<K, V> {
    void onReceive(ConsumerRecords<K, V> records);
  }
  
  @FunctionalInterface
  public interface ErrorHandler {
    void onError(Throwable cause);
  }
  
  private final Consumer<K, V> consumer;
  
  private final long pollTimeoutMillis;
  
  private final RecordHandler<K, V> handler;
  
  private final ErrorHandler errorHandler;
  
  private volatile boolean running = true;
  
  public static ErrorHandler genericErrorLogger(Logger logger) {
    return cause -> logger.warn("Error processing Kafka record", cause);
  }
  
  public KafkaReceiver(Consumer<K, V> consumer, long pollTimeoutMillis, String threadName, 
                       RecordHandler<K, V> handler, ErrorHandler errorHandler) {
    super(threadName);
    this.consumer = consumer;
    this.pollTimeoutMillis = pollTimeoutMillis;
    this.handler = handler;
    this.errorHandler = errorHandler;
    start();
  }
  
  @Override 
  public void run() {
    while (running) {
      final ConsumerRecords<K, V> records;
      try {
        records = consumer.poll(Duration.ofMillis(pollTimeoutMillis));
      } catch (org.apache.kafka.common.errors.InterruptException e) {
        break;
      } catch (Throwable e) {
        errorHandler.onError(e);
        continue;
      }
      if (! records.isEmpty()) {
        handler.onReceive(records);
      }
    }
    consumer.close();
  }
  
  @Override
  public void close() {
    running = false;
  }
  
  public void await() throws InterruptedException {
    join();
  }
}
