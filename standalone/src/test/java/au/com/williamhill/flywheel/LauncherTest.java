package au.com.williamhill.flywheel;

import java.io.*;

import org.junit.*;

import au.com.williamhill.flywheel.Launcher.*;

public final class LauncherTest {
  @Test(expected=LauncherException.class)
  public void testPathDoesNotExit() throws LauncherException {
    new Launcher(new File("ext/foo"));
  }

  @Test(expected=LauncherException.class)
  public void testPathNotADirectory() throws LauncherException {
    new Launcher(new File("ext/conf/default/profile.yaml"));
  }

  @Test(expected=LauncherException.class)
  public void testProfileMissing() throws LauncherException {
    new Launcher(new File("ext"));
  }

  @Test
  public void testDefault() throws LauncherException {
    new Launcher(new File("ext/conf/default"));
  }
}
