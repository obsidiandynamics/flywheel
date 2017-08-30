package au.com.williamhill.flywheel.edge.backplane;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

public final class InVMBackplane implements Backplane {
  private final InVMCluster cluster;
  
  private volatile BackplaneConnector connector;
  
  InVMBackplane(InVMCluster cluster) {
    this.cluster = cluster;
  }

  @Override
  public void attach(BackplaneConnector connector) {
    this.connector = connector;
    cluster.getConnectors().add(connector);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
    if (cluster.getConnectors().size() == 1) return;
    
    for (BackplaneConnector c : cluster.getConnectors()) {
      if (c != connector) {
        c.publish(pub.getTopic(), pub.getPayload());
      }
    }
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    if (cluster.getConnectors().size() == 1) return;
    
    for (BackplaneConnector c : cluster.getConnectors()) {
      if (c != connector) {
        c.publish(pub.getTopic(), pub.getPayload());
      }
    }
  }

  @Override
  public void close() throws Exception {
    cluster.getConnectors().clear();
  }

  @Override
  public String toString() {
    return InVMBackplane.class.getSimpleName();
  }
}
