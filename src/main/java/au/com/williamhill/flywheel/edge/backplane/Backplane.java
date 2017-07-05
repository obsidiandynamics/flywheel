package au.com.williamhill.flywheel.edge.backplane;

import au.com.williamhill.flywheel.edge.*;

/**
 *  An interconnect fabric between broker nodes, such that a message published on one broker
 *  node is made available to all of its peer nodes in a cluster.
 */
public interface Backplane extends AutoCloseable {
  /**
   *  Installs this backplane, attaching it to the local edge node.
   *  
   *  @param edge The node to attach to.
   */
  void attach(EdgeNode edge);
}
