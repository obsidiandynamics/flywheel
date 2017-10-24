package au.com.williamhill.flywheel.edge.auth;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.*;
import au.com.williamhill.flywheel.edge.auth.AuthChain.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;

public final class BindAuthTest extends AbstractAuthTest {
  @SuppressWarnings("resource")
  @Test(expected=NoAuthenticatorException.class)
  public void testEmptySubChain() throws Exception {
    setupEdgeNode(new PubAuthChain(), new SubAuthChain().clear());
  }

  @Test
  public void testDefaultSubChain() throws Exception {
    setupEdgeNode(new PubAuthChain(), new SubAuthChain());
    
    final RemoteNexus remoteNexus = openNexus();
    final String sessionId = generateSessionId();

    // bind to our own RX topic
    final BindFrame bind1 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"a/b/c",
                                                       Flywheel.getRxTopicPrefix(sessionId),
                                                       Flywheel.getRxTopicPrefix(sessionId) + "/#"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind1Res = remoteNexus.bind(bind1).get();
    assertTrue(bind1Res.isSuccess());
    
    // bind to someone else's RX and TX topics
    final BindFrame bind2 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{Flywheel.getRxTopicPrefix(sessionId) + "/#",
                                                       Flywheel.getRxTopicPrefix("12345") + "/#",
                                                       Flywheel.getTxTopicPrefix("12346") + "/#"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind2Res = remoteNexus.bind(bind2).get();
    assertEquals(2, bind2Res.getErrors().length);
    assertEquals(TopicAccessError.class, bind2Res.getErrors()[0].getClass());
    assertEquals(TopicAccessError.class, bind2Res.getErrors()[1].getClass());

    // bind to the TX topic; should pass
    final BindFrame bind3 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"a/b/c",
                                                       Flywheel.getTxTopicPrefix(sessionId),
                                                       Flywheel.getTxTopicPrefix(sessionId) + "/#"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind3Res = remoteNexus.bind(bind3).get();
    assertTrue(bind3Res.isSuccess());
  }
  
  @SuppressWarnings("resource")
  @Test
  public void testCustomSubChain() throws Exception {
    setupEdgeNode(new PubAuthChain(),
                  new SubAuthChain()
                  .set("custom/basic", InterceptingProxy.of(createBasicAuth("user", "pass"), new LoggingInterceptor<>()))
                  .set("custom/bearer", InterceptingProxy.of(createBearerAuth("token"), new LoggingInterceptor<>())));
    
    final RemoteNexus remoteNexus = openNexus();
    final String sessionId = generateSessionId();

    // test with the right user/pass on the correct topic; should pass
    final BindFrame bind1 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          new BasicAuthCredentials("user", "pass"),
                                          new String[]{"a/b/c",
                                                       Flywheel.getRxTopicPrefix(sessionId) + "/#",
                                                       "custom/basic/1"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind1Res = remoteNexus.bind(bind1).get();
    assertTrue(bind1Res.isSuccess());

    // test with a wrong password; should fail
    final BindFrame bind2 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          new BasicAuthCredentials("user", "badpass"),
                                          new String[]{"a/b/c",
                                                       Flywheel.getRxTopicPrefix(sessionId) + "/#",
                                                       "custom/basic/2"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind2Res = remoteNexus.bind(bind2).get();
    assertEquals(1, bind2Res.getErrors().length);
    assertEquals(TopicAccessError.class, bind2Res.getErrors()[0].getClass());

    // test with a wrong password; should fail
    final BindFrame bind3 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          new BasicAuthCredentials("user", "badpass"),
                                          new String[]{"custom/basic"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind3Res = remoteNexus.bind(bind3).get();
    assertEquals(1, bind3Res.getErrors().length);
    assertEquals(TopicAccessError.class, bind3Res.getErrors()[0].getClass());
    
    // test with a null auth object; should fail
    final BindFrame bind4 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"custom/basic/4"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind4Res = remoteNexus.bind(bind4).get();
    assertEquals(1, bind4Res.getErrors().length);
    assertEquals(TopicAccessError.class, bind4Res.getErrors()[0].getClass());

    // test with the right token on the correct topic; should pass
    final BindFrame bind5 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          new BearerAuthCredentials("token"),
                                          new String[]{"custom/bearer/1"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind5Res = remoteNexus.bind(bind5).get();
    assertTrue(bind5Res.isSuccess());

    // bind to a wildcard with the wrong token; should fail with multiple errors
    final BindFrame bind6 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          new BearerAuthCredentials("badtoken"),
                                          new String[]{"#"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind6Res = remoteNexus.bind(bind6).get();
    assertEquals(3, bind6Res.getErrors().length);
    assertEquals(TopicAccessError.class, bind6Res.getErrors()[0].getClass());
    assertEquals(TopicAccessError.class, bind6Res.getErrors()[1].getClass());
    assertEquals(TopicAccessError.class, bind6Res.getErrors()[2].getClass());
  }
}