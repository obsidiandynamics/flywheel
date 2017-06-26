package au.com.williamhill.flywheel.beacon;

public final class RunBeaconRemote {
  public static void main(String[] args) throws Exception {
    new BeaconRemote();
    Thread.sleep(Long.MAX_VALUE);
  }
}
