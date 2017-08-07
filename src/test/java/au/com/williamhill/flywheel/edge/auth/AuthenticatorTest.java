package au.com.williamhill.flywheel.edge.auth;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.mockito.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.Authenticator.*;

public final class AuthenticatorTest {
  @Test
  public void testLifecycle() throws Exception {
    final Authenticator auth = Mockito.mock(Authenticator.class);
    final PubAuthChain chain = new PubAuthChain();
    chain.set("test", auth);
    Mockito.verify(auth).init();
    final List<Authenticator> auths = chain.get("test");
    assertEquals(1, auths.size());
    assertEquals(auth, auths.get(0));
    auths.get(0).verify(null, "test", Mockito.mock(AuthenticationOutcome.class));
    Mockito.verify(auth).verify(Mockito.isNull(EdgeNexus.class), Mockito.eq("test"), 
                                Mockito.notNull(AuthenticationOutcome.class));
    chain.close();
    Mockito.verify(auth).close();
  }
}
