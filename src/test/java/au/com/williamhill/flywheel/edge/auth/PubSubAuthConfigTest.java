package au.com.williamhill.flywheel.edge.auth;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.auth.AuthChain.*;

public final class PubSubAuthConfigTest {
  @Y
  public static final class AuthChainSet {
    @YInject
    PubAuthChain pub;
    
    @YInject
    SubAuthChain sub;
  }
  
  @Test
  public void test() throws IOException {
    final AuthChainSet chains = new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(PubSubAuthConfigTest.class.getClassLoader().getResourceAsStream("auth-chain-config.yaml"))
        .map(AuthChainSet.class);
    
    assertChain(chains.pub);
    assertChain(chains.sub);
  }
  
  private static void assertChain(AuthChain<?> chain) {
    assertNotNull(chain);
    assertNotNull(chain.toString());
    final CombinedMatches matches = chain.getMatches(Collections.singleton("topic"));
    assertNotNull(matches);
    assertEquals(1, matches.numAuthenticators);
    assertEquals(1, matches.matches.size());
    assertNotNull(matches.toString());
  }
}
