package au.com.williamhill.flywheel;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.FlywheelVersion.*;

public final class FlywheelVersionTest {
  @Test
  public void testValid() throws IOException {
    final String version = FlywheelVersion.get();
    assertNotNull(version);
    assertTrue(version.contains("_"));
  }
  
  @Test(expected=IOException.class)
  public void testInvalidWithException() throws IOException {
    FlywheelVersion.get("wrong.file");
  }
  
  @Test
  public void testInvalidWithDefault() throws IOException {
    final String version = FlywheelVersion.get("wrong.file", new Constant("default"));
    assertEquals("default", version);
  }
  
  @Test
  public void testConformance() throws Exception {
    TestSupport.assertUtilityClassWellDefined(FlywheelVersion.class);
  }
}
