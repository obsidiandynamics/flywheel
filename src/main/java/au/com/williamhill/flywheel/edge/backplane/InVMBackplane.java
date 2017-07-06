package au.com.williamhill.flywheel.edge.backplane;

import java.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

public final class InVMBackplane implements Backplane {
  private final List<BackplaneConnector> connectors = new ArrayList<>();

  @Override
  public void attach(BackplaneConnector connector) {
    connectors.add(connector);
  }

  @Override
  public void close() throws Exception {
    connectors.clear();
  }

  @Override
  public void onPublish(BackplaneConnector connector, EdgeNexus nexus, PublishTextFrame pub) {
    if (connectors.size() == 1) return;
    
    for (BackplaneConnector c : connectors) {
      if (c != connector) {
        c.publish(pub.getTopic(), pub.getPayload());
      }
    }
  }

  @Override
  public void onPublish(BackplaneConnector connector, EdgeNexus nexus, PublishBinaryFrame pub) {
    if (connectors.size() == 1) return;
    
    for (BackplaneConnector c : connectors) {
      if (c != connector) {
        c.publish(pub.getTopic(), pub.getPayload());
      }
    }
  }
}
