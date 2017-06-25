package au.com.williamhill.flywheel;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

public final class FlywheelVersionTest {
  @Test
  public void testValid() throws IOException {
    assertNotNull(FlywheelVersion.get());
  }
  
  @Test(expected=IOException.class)
  public void testInvalid() throws IOException {
    FlywheelVersion.get("wrong.file");
  }
}
