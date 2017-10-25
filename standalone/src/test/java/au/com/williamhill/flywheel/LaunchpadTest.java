package au.com.williamhill.flywheel;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.junit.*;
import org.mockito.*;

import au.com.williamhill.flywheel.Launchpad.*;

public final class LaunchpadTest {
  @Before
  public void before() {
    clearProps();
  }
  
  @After
  public void after() {
    clearProps();
  }
  
  private static void clearProps() {
    System.clearProperty("TestLauncher.a");
    System.clearProperty("TestLauncher.b");
    System.clearProperty("flywheel.launchpad.profile");
  }
  
  @Test(expected=LaunchpadException.class)
  public void testPathDoesNotExit() throws LaunchpadException {
    new Launchpad(new File("foo/bar"));
  }

  @Test(expected=LaunchpadException.class)
  public void testPathNotADirectory() throws LaunchpadException {
    new Launchpad(new File("conf/test-good/profile.yaml"));
  }

  @Test(expected=LaunchpadException.class)
  public void testProfileMissing() throws LaunchpadException {
    new Launchpad(new File("conf"));
  }

  @Test(expected=LaunchpadException.class)
  public void testBadProfile() throws LaunchpadException {
    new Launchpad(new File("conf/test-bad"));
  }
  
  @Test(expected=LaunchpadException.class)
  public void testLauncherError() throws Exception {
    final Launchpad launchpad = new Launchpad(new File("conf/test-good"));
    final Launcher launcher = Mockito.mock(Launcher.class);
    Mockito.doThrow(new RuntimeException(new Exception("boom"))).when(launcher).launch(Mockito.any());
    launchpad.getProfile().launchers = new Launcher[] {launcher};
    launchpad.launch(new String[0]);
  }
  
  @Test
  public void testGetProfilePathProperty() {
    System.setProperty("flywheel.launchpad.profile", "conf/test-good");
    final File path = Launchpad.getProfilePath(new HashMap<>());
    assertNotNull(path);
    assertEquals("conf/test-good", path.getPath());
  }
  
  @Test
  public void testGetProfilePathEnvDefault() {
    final File path = Launchpad.getProfilePath(new HashMap<>());
    assertNotNull(path);
    assertEquals("conf/default", path.getPath());
  }
  
  @Test
  public void testGetProfilePathEnvSupplied() {
    final Map<String, String> env = new HashMap<>();
    env.put("FLYWHEEL_PROFILE", "conf/test-good");
    final File path = Launchpad.getProfilePath(env);
    assertNotNull(path);
    assertEquals("conf/test-good", path.getPath());
  }
  
  @Test
  public void testMain() {
    System.setProperty("flywheel.launchpad.profile", "conf/test-good");
    Launchpad.main();
  }
  
  @Test
  public void testBootstrap() {
    final IntConsumer exitHandler = mock(IntConsumer.class);
    System.setProperty("flywheel.launchpad.profile", "conf/test-bad");
    Launchpad.bootstrap(new String[0], exitHandler);
    verify(exitHandler).accept(eq(1));
  }

  @Test
  public void testDirect() throws LaunchpadException {
    final Launchpad launchpad = new Launchpad(new File("conf/test-good"));
    launchpad.launch(new String[0]);
    assertNotNull(launchpad.getProfile().launchers);
    assertEquals(TestLauncher.class, launchpad.getProfile().launchers[0].getClass());
    assertTrue(((TestLauncher) launchpad.getProfile().launchers[0]).launched);
    assertEquals("a", System.getProperty("TestLauncher.a"));
    assertEquals("b", System.getProperty("TestLauncher.b"));
  }
}
