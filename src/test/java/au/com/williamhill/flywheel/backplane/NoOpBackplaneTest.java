package au.com.williamhill.flywheel.backplane;

import org.junit.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;

public final class NoOpBackplaneTest extends BackplaneTest {
  @Override
  protected Backplane getBackplane() throws Exception {
    return new NoOpBackplane();
  }
  
  @Test
  public void testSingleNode() throws Exception {
    final int nodes = 1;
    final int subscribersPerNode = 5;
    final int messages = 10;
    testCrossCluster(nodes, subscribersPerNode, messages, messages);
  }
  
  @Test
  public void testMultiNode() throws Exception {
    final int nodes = 4;
    final int subscribersPerNode = 5;
    final int messages = 10;
    testCrossCluster(nodes, subscribersPerNode, messages, messages);
  }
}
