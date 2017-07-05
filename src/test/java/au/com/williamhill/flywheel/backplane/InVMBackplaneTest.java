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
    final int messages = 10 * SCALE;
    final int expectedMessages = messages;
    testCrossCluster(CYCLES, false, nodes, subscribersPerNode, messages, expectedMessages);
    testCrossCluster(CYCLES, true, nodes, subscribersPerNode, messages, expectedMessages);
  }
  
  @Test
  public void testMultiNode() throws Exception {
    final int nodes = 4;
    final int subscribersPerNode = 5 * SCALE;
    final int messages = 10 * SCALE;
    final int expectedMessages = messages * nodes;
    testCrossCluster(CYCLES, false, nodes, subscribersPerNode, messages, expectedMessages);
    testCrossCluster(CYCLES, true, nodes, subscribersPerNode, messages, expectedMessages);
  }
}
