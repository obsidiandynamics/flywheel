package au.com.williamhill.flywheel.edge.backplane;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

/**
 *  A no-op {@link Backplane} implementation for use with single-node (cluster-less) brokers.
 */
@Y
public final class NoOpBackplane implements Backplane {
  @Override
  public void close() throws Exception {}

  @Override
  public void attach(BackplaneConnector connector) {}

  @Override
  public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {}

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {}

  @Override
  public String toString() {
    return NoOpBackplane.class.getSimpleName();
  }
}
