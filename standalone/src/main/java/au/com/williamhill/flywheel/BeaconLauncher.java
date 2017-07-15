package au.com.williamhill.flywheel;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.beacon.*;

@Y
public final class BeaconLauncher implements Launcher {
  @Override
  public void launch(String[] args, Profile profile) throws Exception {
    new BeaconEdge();
  }
}
