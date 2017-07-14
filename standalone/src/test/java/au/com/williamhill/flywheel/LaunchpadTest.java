package au.com.williamhill.flywheel;

import static junit.framework.TestCase.*;

import java.io.*;

import org.junit.*;

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
  }
  
  @Test(expected=LaunchpadException.class)
  public void testPathDoesNotExit() throws LaunchpadException {
    new Launchpad(new File("foo/bar")).launch(new String[0]);
  }

  @Test(expected=LaunchpadException.class)
  public void testPathNotADirectory() throws LaunchpadException {
    new Launchpad(new File("conf/test/profile.yaml")).launch(new String[0]);
  }

  @Test(expected=LaunchpadException.class)
  public void testProfileMissing() throws LaunchpadException {
    new Launchpad(new File("conf")).launch(new String[0]);
  }

  @Test
  public void testDefault() throws LaunchpadException {
    final Launchpad launchpad = new Launchpad(new File("conf/test"));
    launchpad.launch(new String[0]);
    assertNotNull(launchpad.getProfile().launcher);
    assertEquals(TestLauncher.class, launchpad.getProfile().launcher.getClass());
    assertTrue(((TestLauncher) launchpad.getProfile().launcher).launched);
    assertEquals("a", System.getProperty("TestLauncher.a"));
    assertEquals("b", System.getProperty("TestLauncher.b"));
  }
}
