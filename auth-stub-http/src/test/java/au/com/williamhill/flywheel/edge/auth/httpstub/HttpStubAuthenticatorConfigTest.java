package au.com.williamhill.flywheel.edge.auth.httpstub;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.junit.*;

import com.obsidiandynamics.yconf.*;

public final class HttpStubAuthenticatorConfigTest {
  @Test
  public void test() throws IOException, URISyntaxException {
    try (HttpStubAuthenticator auth = new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(HttpStubAuthenticatorConfigTest.class.getClassLoader().getResourceAsStream("http-stub-auth-config.yaml"))
        .map(HttpStubAuthenticator.class)) {
      assertEquals(new URI("http://localhost:8090/auth"), auth.getConfig().uri);
      assertEquals(4, auth.getConfig().poolSize);
      assertEquals(30000, auth.getConfig().timeoutMillis);
      assertNotNull(auth.toString());
      assertNotNull(auth.getConfig().toString());
    }
  }
}
