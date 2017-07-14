package au.com.williamhill.flywheel;

import java.io.*;

import org.junit.*;

import au.com.williamhill.flywheel.Launchpad.*;

public final class LaunchpadTest {
  @Test(expected=LauncherException.class)
  public void testPathDoesNotExit() throws LauncherException {
    new Launchpad(new File("foo/bar"));
  }

  @Test(expected=LauncherException.class)
  public void testPathNotADirectory() throws LauncherException {
    new Launchpad(new File("conf/test/profile.yaml"));
  }

  @Test(expected=LauncherException.class)
  public void testProfileMissing() throws LauncherException {
    new Launchpad(new File("conf"));
  }

  @Test
  public void testDefault() throws LauncherException {
    new Launchpad(new File("conf/test"));
  }
}
