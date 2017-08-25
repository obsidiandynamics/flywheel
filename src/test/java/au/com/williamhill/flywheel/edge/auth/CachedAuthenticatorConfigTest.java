package au.com.williamhill.flywheel.edge.auth;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.yconf.*;

public final class CachedAuthenticatorConfigTest {
  @Test
  public void test() throws Exception {
    try (CachedAuthenticator auth = new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(CachedAuthenticatorConfigTest.class.getClassLoader().getResourceAsStream("cached-authenticator-config.yaml"))
        .map(CachedAuthenticator.class)) {
      assertNotNull(auth.toString());
    }
  }
}
