package au.com.williamhill.flywheel.backplane;

import org.junit.*;

import au.com.williamhill.flywheel.edge.backplane.*;

public final class InVMBackplaneTest extends BackplaneTest {
  private static final int CYCLES = 2;
  private static final int SCALE = 1;
  
  @Override
  protected Backplane getBackplane() throws Exception {
    return new InVMBackplane();
  }
  
  @Test
  public void testSingleNode() throws Exception {
    final int nodes = 1;
    final int subscribersPerNode = 5 * SCALE;
    final int topics = 3;
    final int messagesPerTopic = 10 * SCALE;
    final int expectedPartitions = nodes * topics;
    final int expectedMessages = topics * messagesPerTopic;
    testCrossCluster(CYCLES, false, nodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
    testCrossCluster(CYCLES, true, nodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
  }
  
  @Test
  public void testMultiNode() throws Exception {
    final int nodes = 4;
    final int subscribersPerNode = 5 * SCALE;
    final int topics = 3;
    final int messagesPerTopic = 10 * SCALE;
    final int expectedPartitions = nodes * topics;
    final int expectedMessages = nodes * topics * messagesPerTopic;
    testCrossCluster(CYCLES, false, nodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
    testCrossCluster(CYCLES, true, nodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
  }
}
