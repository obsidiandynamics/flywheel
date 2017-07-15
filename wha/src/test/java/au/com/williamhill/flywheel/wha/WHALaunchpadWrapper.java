package au.com.williamhill.flywheel.wha;

import au.com.williamhill.flywheel.*;

public final class WHALaunchpadWrapper {
  public static void main(String... args) {
    System.setProperty("flywheel.launchpad.profile", "conf/wha");
    Launchpad.main(args);
  }
}
