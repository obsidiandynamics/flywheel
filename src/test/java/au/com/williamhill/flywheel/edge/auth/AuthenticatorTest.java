package au.com.williamhill.flywheel.edge.auth;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.mockito.*;

import au.com.williamhill.flywheel.edge.auth.NestedAuthenticator.*;

public final class AuthenticatorTest {
  @Test
  public void testLifecycle() throws Exception {
    final Authenticator auth = Mockito.mock(Authenticator.class);
    final PubAuthChain chain = new PubAuthChain();
    chain.set("test", auth);
    auth.attach(Mockito.mock(AuthConnector.class));
    Mockito.verify(auth).attach(Mockito.any());
    final List<Authenticator> auths = chain.get("test");
    assertEquals(1, auths.size());
    assertEquals(auth, auths.get(0));
    auths.get(0).verify(null, "test", Mockito.mock(AuthenticationOutcome.class));
    Mockito.verify(auth).verify(Mockito.isNull(), Mockito.eq("test"), 
                                Mockito.notNull());
    chain.close();
    Mockito.verify(auth).close();
  }
}
