package au.com.williamhill.flywheel.socketx;

import static junit.framework.TestCase.*;

import javax.net.ssl.*;

import org.junit.*;

import au.com.williamhill.flywheel.socketx.ssl.*;

public final class XEndpointConfigTest {
  private static final class DerivedConfig extends XEndpointConfig<DerivedConfig> {};
  
  @Test
  public void testHighWaterMark() {
    assertEquals(1000, new DerivedConfig().withHighWaterMark(1000).highWaterMark);
  }
  
  @Test
  public void testSSLContextProvider() {
    class TestSSLContextProvider implements SSLContextProvider {
      @Override public SSLContext getSSLContext() throws Exception {
        return null;
      }
    }
    assertEquals(TestSSLContextProvider.class, 
                 new DerivedConfig()
                 .withSSLContextProvider(new TestSSLContextProvider()).sslContextProvider.getClass());
  }
}
