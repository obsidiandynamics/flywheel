package au.com.williamhill.flywheel.edge.backplane.kafka;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.*;
import org.apache.kafka.common.errors.*;
import org.junit.*;
import org.mockito.stubbing.*;
import org.slf4j.*;

import com.obsidiandynamics.socketx.util.*;

import au.com.williamhill.flywheel.edge.backplane.kafka.KafkaReceiver.*;


public final class KafkaReceiverTest {
  private KafkaReceiver<String, String> receiver;
  private Consumer<String, String> consumer;
  private RecordHandler<String, String> recordHandler;
  private ErrorHandler errorHandler;
  
  @Before
  @SuppressWarnings("unchecked")
  public void before() {
    consumer = mock(Consumer.class);
    recordHandler = mock(RecordHandler.class);
    errorHandler = mock(ErrorHandler.class);
  }
  
  @After
  public void after() throws InterruptedException {
    if (receiver != null) receiver.close();
  }
  
  private static Answer<?> split(Supplier<ConsumerRecords<String, String>> first) {
    return split(first, first);
  }
  
  private static Answer<?> split(Supplier<ConsumerRecords<String, String>> first,
                                 Supplier<ConsumerRecords<String, String>> others) {
    final AtomicBoolean firstCall = new AtomicBoolean();
    return invocation -> {
      if (firstCall.compareAndSet(false, true)) {
        return first.get();
      } else {
        final long timeout = (Long) invocation.getArguments()[0];
        Thread.sleep(timeout);
        return others.get();
      }
    };
  }
  
  @Test
  public void testReceive() {
    final Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = 
        Collections.singletonMap(new TopicPartition("test", 0), Arrays.asList(new ConsumerRecord<>("test", 0, 0, "key", "value")));
    final ConsumerRecords<String, String> records = new ConsumerRecords<>(recordsMap);
    
    when(consumer.poll(anyLong())).then(split(() -> records, 
                                              () -> new ConsumerRecords<>(Collections.emptyMap())));
    receiver = new KafkaReceiver<String, String>(consumer, 1, "TestThread", recordHandler, errorHandler);
    SocketUtils.await().until(() -> {
      verify(recordHandler, times(1)).onReceive(eq(records));
      verify(errorHandler, never()).onError(any());
    });
  }

  @Test
  public void testInterrupt() throws InterruptedException {
    when(consumer.poll(anyLong())).then(split(() -> { throw createInterruptException(); }));
    receiver = new KafkaReceiver<String, String>(consumer, 1, "TestThread", recordHandler, errorHandler);
    verify(recordHandler, never()).onReceive(any());
    verify(errorHandler, never()).onError(any());
    receiver.await();
  }
  
  @Test
  public void testError() throws InterruptedException {
    when(consumer.poll(anyLong())).then(split(() -> { throw new RuntimeException("boom"); }));
    receiver = new KafkaReceiver<String, String>(consumer, 1, "TestThread", recordHandler, errorHandler);
    SocketUtils.await().until(() -> {
      verify(recordHandler, never()).onReceive(any());
      verify(errorHandler, atLeastOnce()).onError(any(RuntimeException.class));
    });
    receiver.close();
    receiver.await();
    verify(consumer).close();
  }

  @Test
  public void testGenericErrorLogger() {
    when(consumer.poll(anyLong())).then(split(() -> { throw new RuntimeException("boom"); }));
    final Logger logger = mock(Logger.class);
    receiver = new KafkaReceiver<String, String>(consumer, 1, "TestThread", recordHandler, KafkaReceiver.genericErrorLogger(logger));
    SocketUtils.await().until(() -> {
      verify(recordHandler, never()).onReceive(any());
      verify(logger, atLeastOnce()).warn(anyString(), any(RuntimeException.class));
    });
  }
  
  private static InterruptException createInterruptException() {
    final InterruptException ie = new InterruptException("Interrupted");
    Thread.interrupted();
    return ie;
  }
}
