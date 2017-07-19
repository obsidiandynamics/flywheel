package au.com.williamhill.flywheel.beacon;

public final class RunBeaconRemote {
  public static void main(String[] args) throws Exception {
    System.setProperty("log4j.configuration", "file:conf/default/log4j-default.properties");
    new BeaconRemote();
    Thread.sleep(Long.MAX_VALUE);
  }
}
