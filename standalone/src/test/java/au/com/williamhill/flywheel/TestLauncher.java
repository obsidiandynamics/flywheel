package au.com.williamhill.flywheel;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.yconf.*;

@Y(YReflectiveMapper.class)
public final class TestLauncher implements Launcher {
  boolean launched;
  
  @Override
  public void launch(String[] args, Profile profile) throws Exception {
    launched = true;
  }
}
