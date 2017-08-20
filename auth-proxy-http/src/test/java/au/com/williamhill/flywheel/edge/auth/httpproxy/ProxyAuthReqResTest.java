package au.com.williamhill.flywheel.edge.auth.httpproxy;

import static org.junit.Assert.*;

import org.junit.*;

import au.com.williamhill.flywheel.frame.*;

public final class ProxyAuthReqResTest {
  @Test
  public void testRequest() {
    final BasicAuth auth = new BasicAuth("user", "pass");
    final ProxyAuthRequest req = new ProxyAuthRequest(auth, "topic");
    assertEquals(auth, req.getAuth());
    assertEquals("topic", req.getTopic());
    assertNotNull(req.toString());
  }
  
  @Test
  public void testResponse() {
    final ProxyAuthResponse res = new ProxyAuthResponse(1000L);
    assertEquals(1000L, (long) res.getAllowMillis());
    assertNotNull(res.toString());
  }
}
