package au.com.williamhill.flywheel.beacon;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.socketx.*;

public final class RunBeaconEdge {
  public static void main(String[] args) throws Exception {
    System.setProperty("log4j.configuration", "file:conf/default/log4j-default.properties");
    EdgeNode.builder()
    .withServerConfig(new XServerConfig().withPath("/broker"))
    .withPlugins(new Beacon())
    .build();
  }
}
