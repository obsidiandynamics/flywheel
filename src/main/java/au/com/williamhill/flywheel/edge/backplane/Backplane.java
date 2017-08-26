package au.com.williamhill.flywheel.edge.backplane;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

/**
 *  An interconnect fabric between broker nodes, such that a message published on one broker
 *  node is made available to all of its peer nodes in a cluster.
 */
public interface Backplane extends AutoCloseable {
  /**
   *  Installs this backplane, attaching it to the node-local connector.
   *  
   *  @param connector The connector to attach to.
   *  @throws Exception If an error occurs.
   */
  void attach(BackplaneConnector connector) throws Exception;
  
  /**
   *  Invoked when a text message is published on the local node. The backplane is responsible
   *  for disseminating this message to the peer nodes.
   *  
   *  @param nexus The publisher nexus.
   *  @param pub The published frame.
   */
  void onPublish(EdgeNexus nexus, PublishTextFrame pub);

  /**
   *  Invoked when a binary message is published on the local node. The backplane is responsible
   *  for disseminating this message to the peer nodes.
   *  
   *  @param nexus The publisher nexus.
   *  @param pub The published frame.
   */
  void onPublish(EdgeNexus nexus, PublishBinaryFrame pub);
}
