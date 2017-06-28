package au.com.williamhill.flywheel;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

public final class FlywheelVersionTest {
  @Test
  public void testValid() throws IOException {
    final String version = FlywheelVersion.get();
    assertNotNull(version);
    assertTrue(version.contains("_"));
  }
  
  @Test(expected=IOException.class)
  public void testInvalid() throws IOException {
    FlywheelVersion.get("wrong.file");
  }
}
