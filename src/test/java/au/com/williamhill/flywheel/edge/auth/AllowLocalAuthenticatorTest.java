package au.com.williamhill.flywheel.edge.auth;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.Authenticator.*;

public final class AllowLocalAuthenticatorTest {
  @Test
  public void testAllowLocal() {
    final Authenticator<?> auth = AllowLocalAuthenticator.instance();
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    auth.verify(new EdgeNexus(null, LocalPeer.instance()), "topic", outcome);
    verify(outcome).allow(eq(AuthenticationOutcome.INDEFINITE));
  }
  @Test
  public void testDenyNonLocal() {
    final Authenticator<?> auth = AllowLocalAuthenticator.instance();
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    auth.verify(new EdgeNexus(null, mock(Peer.class)), "topic", outcome);
    verify(outcome).forbidden(eq("topic"));
  }
}
