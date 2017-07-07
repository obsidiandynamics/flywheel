package au.com.williamhill.flywheel.edge.backplane;

import org.junit.*;

public final class InVMClusterTest extends ClusterTest {
  private static final int CYCLES = 2;
  private static final int SCALE = 1;
  
  private InVMCluster cluster;
  
  @Override
  protected void init() throws Exception {
    super.init();
    cluster = new InVMCluster();
  }
  
  @Override
  protected Backplane getBackplane(String clusterId, String brokerId) throws Exception {
    return cluster.createBackplane();
  }
  
  @Test
  public void testSingleNode() throws Exception {
    final int nodes = 1;
    final int subscribersPerNode = 5 * SCALE;
    final int topics = 3;
    final int messagesPerTopic = 10 * SCALE;
    final int expectedPartitions = nodes * topics;
    final int expectedMessages = topics * messagesPerTopic;
    test(CYCLES, false, nodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
    test(CYCLES, true, nodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
  }
  
  @Test
  public void testMultiNode() throws Exception {
    final int nodes = 4;
    final int subscribersPerNode = 5 * SCALE;
    final int topics = 3;
    final int messagesPerTopic = 10 * SCALE;
    final int expectedPartitions = nodes * topics;
    final int expectedMessages = nodes * topics * messagesPerTopic;
    test(CYCLES, false, nodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
    test(CYCLES, true, nodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
  }
}
