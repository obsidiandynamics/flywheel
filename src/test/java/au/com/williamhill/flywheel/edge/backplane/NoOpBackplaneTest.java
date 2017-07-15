package au.com.williamhill.flywheel.edge.backplane;

import org.junit.*;

public final class NoOpBackplaneTest extends BackplaneTest {
  private static final int CYCLES = 2;
  private static final int SCALE = 1;
  
  @Override
  protected Backplane getBackplane(String clusterId, String brokerId) throws Exception {
    return new NoOpBackplane();
  }
  
  @Test
  public void testSingleNode() throws Exception {
    final int connectors = 1;
    final int topics = 3;
    final int messagesPerTopic = 10 * SCALE;
    final int expectedPartitions = 0;
    final int expectedMessages = 0;
    test(CYCLES, false, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
    test(CYCLES, true, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
  }
  
  @Test
  public void testMultiNode() throws Exception {
    final int connectors = 4;
    final int topics = 3;
    final int messagesPerTopic = 10 * SCALE;
    final int expectedPartitions = 0;
    final int expectedMessages = 0;
    test(CYCLES, false, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
    test(CYCLES, true, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
  }
}
