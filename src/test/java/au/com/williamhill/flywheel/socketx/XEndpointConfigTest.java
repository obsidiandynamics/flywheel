package au.com.williamhill.flywheel.socketx;

import static junit.framework.TestCase.*;

import org.junit.*;

public final class XEndpointConfigTest {
  @Test
  public void testHighWaterMark() {
    class DerivedConfig extends XEndpointConfig<DerivedConfig> {};
    assertEquals(1000, new DerivedConfig().withHighWaterMark(1000).highWaterMark);
  }
}
