package au.com.williamhill.flywheel.edge.auth.httpstub;

import static org.junit.Assert.*;

import org.junit.*;

import au.com.williamhill.flywheel.frame.*;

public final class StubAuthReqResTest {
  @Test
  public void testRequest() {
    final BasicAuthCredentials auth = new BasicAuthCredentials("user", "pass");
    final StubAuthRequest req = new StubAuthRequest(auth, "topic");
    assertEquals(auth, req.getCredentials());
    assertEquals("topic", req.getTopic());
    assertNotNull(req.toString());
  }
  
  @Test
  public void testResponse() {
    final StubAuthResponse res = new StubAuthResponse(1000L);
    assertEquals(1000L, (long) res.getAllowMillis());
    assertNotNull(res.toString());
  }
}
