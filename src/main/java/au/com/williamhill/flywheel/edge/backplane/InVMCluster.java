package au.com.williamhill.flywheel.edge.backplane;

import java.util.*;

public final class InVMCluster {
  private final List<BackplaneConnector> connectors = new ArrayList<>();
  
  List<BackplaneConnector> getConnectors() {
    return connectors;
  }
  
  public InVMBackplane createBackplane() {
    return new InVMBackplane(this);
  }
}
