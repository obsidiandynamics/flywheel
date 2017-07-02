package au.com.williamhill.flywheel.socketx;

import static junit.framework.TestCase.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

public final class XEndpointConfigTest {
  @Test
  public void testToString() {
    TestSupport.assertToString(new XEndpointConfig());
  }
  
  @Test
  public void testHighWaterMark() {
    assertEquals(1000, new XEndpointConfig().withHighWaterMark(1000).highWaterMark);
  }
}
