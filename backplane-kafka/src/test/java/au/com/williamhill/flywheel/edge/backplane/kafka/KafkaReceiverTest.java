package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;
import java.util.concurrent.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;
import org.apache.kafka.common.errors.*;
import org.awaitility.*;
import org.junit.*;
import org.mockito.*;
import org.slf4j.*;

import au.com.williamhill.flywheel.edge.backplane.kafka.KafkaReceiver.*;


public final class KafkaReceiverTest {
  private KafkaReceiver<String, String> receiver;
  private Consumer<String, String> consumer;
  private RecordHandler<String, String> recordHandler;
  private ErrorHandler errorHandler;
  
  @Before
  @SuppressWarnings("unchecked")
  public void before() {
    consumer = Mockito.mock(Consumer.class);
    recordHandler = Mockito.mock(RecordHandler.class);
    errorHandler = Mockito.mock(ErrorHandler.class);
  }
  
  @After
  public void after() throws InterruptedException {
    if (receiver != null) receiver.close();
  }
  
  @Test
  public void testReceive() {
    final Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = 
        Collections.singletonMap(new TopicPartition("test", 0), Arrays.asList(new ConsumerRecord<>("test", 0, 0, "key", "value")));
    final ConsumerRecords<String, String> records = new ConsumerRecords<>(recordsMap);
    Mockito.when(consumer.poll(Mockito.anyLong())).thenReturn(records);
    receiver = new KafkaReceiver<String, String>(consumer, 1, "TestThread", recordHandler, errorHandler);
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(recordHandler, Mockito.atLeastOnce()).onReceive(Mockito.eq(records));
      Mockito.verify(errorHandler, Mockito.never()).onError(Mockito.any());
    });
  }

  @Test
  public void testInterrupt() throws InterruptedException {
    Mockito.when(consumer.poll(Mockito.anyLong())).thenThrow(createInterruptException());
    receiver = new KafkaReceiver<String, String>(consumer, 1, "TestThread", recordHandler, errorHandler);
    Mockito.verify(recordHandler, Mockito.never()).onReceive(Mockito.any());
    Mockito.verify(errorHandler, Mockito.never()).onError(Mockito.any());
    receiver.await();
  }
  
  @Test
  public void testError() throws InterruptedException {
    Mockito.when(consumer.poll(Mockito.anyLong())).thenThrow(new RuntimeException("boom"));
    receiver = new KafkaReceiver<String, String>(consumer, 1, "TestThread", recordHandler, errorHandler);
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(recordHandler, Mockito.never()).onReceive(Mockito.any());
      Mockito.verify(errorHandler, Mockito.atLeastOnce()).onError(Mockito.any(RuntimeException.class));
    });
    receiver.close();
    receiver.await();
    Mockito.verify(consumer).close();
  }

  @Test
  public void testGenericErrorLogger() {
    Mockito.when(consumer.poll(Mockito.anyLong())).thenThrow(new RuntimeException("boom"));
    final Logger logger = Mockito.mock(Logger.class);
    receiver = new KafkaReceiver<String, String>(consumer, 1, "TestThread", recordHandler, KafkaReceiver.genericErrorLogger(logger));
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(recordHandler, Mockito.never()).onReceive(Mockito.any());
      Mockito.verify(logger, Mockito.atLeastOnce()).warn(Mockito.anyString(), Mockito.any(RuntimeException.class));
    });
  }
  
  private static InterruptException createInterruptException() {
    final InterruptException ie = new InterruptException("Interrupted");
    Thread.interrupted();
    return ie;
  }
}
