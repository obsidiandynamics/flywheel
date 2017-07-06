package au.com.williamhill.flywheel.edge;

import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.frame.*;

/**
 *  A no-op {@link Backplane} implementation for single-node (cluster-less) brokers.
 */
public final class NoOpBackplane implements Backplane {
  @Override
  public void close() throws Exception {}

  @Override
  public void attach(BackplaneConnector connector) {}

  @Override
  public void onPublish(BackplaneConnector connector, EdgeNexus nexus, PublishTextFrame pub) {}

  @Override
  public void onPublish(BackplaneConnector connector, EdgeNexus nexus, PublishBinaryFrame pub) {}
}
