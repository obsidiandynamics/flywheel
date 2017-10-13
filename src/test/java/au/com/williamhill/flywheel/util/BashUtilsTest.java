package au.com.williamhill.flywheel.util;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

//TODO remove
public final class BashUtilsTest {
  @Test
  public void testConformance() throws Exception {
    TestSupport.assertUtilityClassWellDefined(BashUtils.class);
  }
  
  @Test
  public void testNotInstalled() {
    try {
      BashUtils.checkInstalled("XYZ", "xyz_not_found");
      fail("Failed to throw an AssertionError");
    } catch (AssertionError e) {
      assertTrue("e=" + e, e.getMessage().startsWith("XYZ is not installed"));
    }
  }
  
  @Test
  public void testRunError() {
    try {
      BashUtils.checkInstalled("XYZ", "ls -?");
      fail("Failed to throw an AssertionError");
    } catch (AssertionError e) {
      assertTrue("e=" + e, e.getMessage().startsWith("Error running XYZ"));
    }
  }
}
