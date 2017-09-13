package au.com.williamhill.flywheel.edge.backplane;

import static junit.framework.TestCase.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.util.*;

public abstract class BackplaneTest implements TestSupport {
  private static final String TOPIC_PREFIX = "topics/";

  protected abstract Backplane getBackplane(String clusterId, String brokerId) throws Exception;
  
  @Before
  public final void before() throws Exception {
    init();
  }
  
  protected void init() throws Exception {}

  @After
  public final void after() throws Exception {
    cleanup();
  }

  protected void cleanup() throws Exception {}

  private static final class MockConnector implements BackplaneConnector, TestSupport {
    final String brokerId;
    final Backplane backplane;
    final KeyedBlockingQueue<String, TestBackplaneMessage> received = 
        new KeyedBlockingQueue<>(LinkedBlockingQueue::new);

    MockConnector(String brokerId, Backplane backplane) throws Exception {
      this.brokerId = brokerId;
      this.backplane = backplane;
      backplane.attach(this);
    }

    @Override
    public void publish(String topic, String payload) {
      log("s: received (text) %s, topic: %s\n", payload, topic);
      final TestBackplaneMessage msg = TestBackplaneMessage.fromString(payload);
      received.forKey(getKey(msg.brokerId, topic)).add(msg);
    }

    @Override
    public void publish(String topic, byte[] payload) {
      final String str = new String(payload);
      log("s: received (binary) %s, topic: %s\n", str, topic);
      final TestBackplaneMessage msg = TestBackplaneMessage.fromString(str);
      received.forKey(getKey(msg.brokerId, topic)).add(msg);
    }

    private static String getKey(String brokerId, String topic) {
      return brokerId + "_" + topic;
    }
  }

  protected final void test(int cycles,
                            boolean binary,
                            int connectors, 
                            int topics,
                            int messagesPerTopic, 
                            int expectedPartitions,
                            int expectedMessages) throws Exception {
    for (int i = 0; i < cycles; i++) {
      init();
      try {
        test(binary, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
      } finally {
        cleanup();
      }
    }
  }

  private void test(boolean binary,
                    int connectors, 
                    int topics,
                    int messagesPerTopic, 
                    int expectedPartitions,
                    int expectedMessages) throws Exception {
    final String clusterId = "TestCluster-" + System.currentTimeMillis();
    final List<MockConnector> mockConnectors = new ArrayList<>(connectors);

    for (int i = 0; i < connectors; i++) {
      final String brokerId = String.valueOf(i);
      final Backplane backplane = getBackplane(clusterId, brokerId);
      mockConnectors.add(new MockConnector(brokerId, backplane));
    }

    final XEndpointPeer peer = new XEndpointPeer(Mockito.mock(XEndpoint.class));
    ParallelJob.blockingSlice(mockConnectors, Runtime.getRuntime().availableProcessors(), connectorsSlice -> {
      for (MockConnector conn : connectorsSlice) {
        for (int m = 0; m < messagesPerTopic; m++) {
          final TestBackplaneMessage message = new TestBackplaneMessage(conn.brokerId, m);
          final EdgeNexus nexus = new EdgeNexus(null, peer);
          for (int t = 0; t < topics; t++) {
            if (binary) {
              conn.backplane.onPublish(nexus, new PublishBinaryFrame(TOPIC_PREFIX + t, message.toString().getBytes()));
            } else {
              conn.backplane.onPublish(nexus, new PublishTextFrame(TOPIC_PREFIX + t, message.toString()));
            }
          }
        }
      }
    }).run();
    log("publishing complete\n");

    try {
      SocketTestSupport.await().untilTrue(() -> mockConnectors.stream().filter(c -> c.received.totalSize() < expectedMessages).count() == 0);
    } finally {
      for (MockConnector conn : mockConnectors) {
        assertEquals(expectedPartitions, conn.received.asMap().size());
        assertEquals(expectedMessages, conn.received.totalSize());
        for (Map.Entry<String, BlockingQueue<TestBackplaneMessage>> entry : conn.received.asMap().entrySet()) {
          final List<TestBackplaneMessage> messages = new ArrayList<>(entry.getValue());
          assertEquals(messagesPerTopic, messages.size());
          for (int m = 0; m < messagesPerTopic; m++) {
            final TestBackplaneMessage message = messages.get(m);
            assertEquals(m, message.messageId);
          }
        }
      }
    }
  }
}
