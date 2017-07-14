package au.com.williamhill.flywheel;

import au.com.williamhill.flywheel.yconf.*;

@FunctionalInterface
public interface Launcher {
  void launch(String[] args, Profile profile) throws Exception;
}
