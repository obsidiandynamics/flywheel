package au.com.williamhill.flywheel.edge;

import au.com.williamhill.flywheel.edge.backplane.*;

/**
 *  A no-op {@link Backplane} implementation for single-node (cluster-less) brokers.
 */
public final class NoOpBackplane implements Backplane {
  @Override
  public void close() throws Exception {}

  @Override
  public void attach(EdgeNode edge) {}
}
