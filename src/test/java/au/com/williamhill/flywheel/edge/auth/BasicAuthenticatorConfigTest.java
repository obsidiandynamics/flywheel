package au.com.williamhill.flywheel.edge.auth;

import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.Authenticator.*;

public final class BasicAuthenticatorConfigTest {
  @Y(AuthenticatorSet.Mapper.class)
  public static final class AuthenticatorSet extends HashSet<Authenticator<?>> {
    private static final long serialVersionUID = 1L;

    public static final class Mapper implements TypeMapper {
      @Override public Object map(YObject y, Class<?> type) {
        return y.asList().stream()
            .<Authenticator<?>>map(o -> o.map(Authenticator.class))
            .collect(Collectors.toCollection(AuthenticatorSet::new));
      }
    }
  }
  
  @Test
  public void test() throws Exception {
    final AuthenticatorSet auths = new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(BasicAuthenticatorConfigTest.class.getClassLoader().getResourceAsStream("basic-authenticator-config.yaml"))
        .map(AuthenticatorSet.class);
    
    assertEquals(4, auths.size());
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    final AuthConnector connector = Mockito.mock(AuthConnector.class);
    for (Authenticator<?> auth : auths) {
      assertNotNull(auth.toString());
      @SuppressWarnings("unchecked")
      final Authenticator<AuthConnector> aa = (Authenticator<AuthConnector>) auth;
      aa.attach(connector);
      aa.attach(connector);
      auth.verify(nexus, "topic", Mockito.mock(AuthenticationOutcome.class));
      auth.close();
      auth.close();
    }
  }
}
