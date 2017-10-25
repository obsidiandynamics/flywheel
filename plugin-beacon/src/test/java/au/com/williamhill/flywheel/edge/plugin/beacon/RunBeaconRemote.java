package au.com.williamhill.flywheel.edge.plugin.beacon;

public final class RunBeaconRemote {
  public static void main(String[] args) throws Exception {
    System.setProperty("log4j.configuration", "file:../standalone/conf/default/log4j-default.properties");
    new BeaconRemote();
    Thread.sleep(Long.MAX_VALUE);
  }
}
