package au.com.williamhill.flywheel.edge.auth.httpproxy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.*;
import java.util.*;

import org.junit.*;

import com.github.tomakehurst.wiremock.junit.*;
import com.google.gson.*;

import au.com.williamhill.flywheel.edge.auth.*;
import au.com.williamhill.flywheel.edge.auth.NestedAuthenticator.*;
import au.com.williamhill.flywheel.edge.auth.httpproxy.util.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;

public final class HttpProxyAuthUncachedPubTest extends AbstractAuthTest {
  private static final boolean USE_HTTPS = false;

  private static final String MOCK_PATH = "/auth";

  private static final String TOPIC = "test";

  @ClassRule
  public static WireMockRule wireMock = new WireMockRule(options()
                                                         .dynamicPort()
                                                         .dynamicHttpsPort());

  @Override
  protected void setup() throws URISyntaxException, Exception {
    reset();
    setupAuthChains();
  }

  private URI getURI(boolean https) throws URISyntaxException {
    return new WireMockURIBuilder()
        .withWireMock(wireMock)
        .withHttps(https)
        .withPath(MOCK_PATH)
        .build();
  }

  @SuppressWarnings("resource")
  private void setupAuthChains() throws URISyntaxException, Exception {
    setupEdgeNode(new PubAuthChain().set(TOPIC, 
                                         new AuthenticatorWrapper(new HttpProxyAuth(new HttpProxyAuthConfig()
                                                                                    .withURI(getURI(USE_HTTPS))))),
                  new SubAuthChain());
  }

  @Test
  public void testAllow() throws Exception {
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(new ProxyAuthResponse(AuthenticationOutcome.INDEFINITE)))));

    final RemoteNexus remoteNexus = openNexus();
    final String sessionId = generateSessionId();

    final BindFrame bind = new BindFrame(UUID.randomUUID(), 
                                         sessionId,
                                         null,
                                         new String[]{TOPIC},
                                         new String[]{},
                                         null);
    final BindResponseFrame bindRes = remoteNexus.bind(bind).get();
    assertTrue(bindRes.isSuccess());
    verify(0, postRequestedFor(urlMatching(MOCK_PATH)));

    remoteNexus.publish(new PublishTextFrame(TOPIC, "hello"));
    awaitReceived();
    assertNull(errors);
    assertEquals(new TextFrame(TOPIC, "hello"), text);
    clearReceived();
    verify(1, postRequestedFor(urlMatching(MOCK_PATH)));

    remoteNexus.publish(new PublishTextFrame(TOPIC, "hello"));
    awaitReceived();
    assertNull(errors);
    assertEquals(new TextFrame(TOPIC, "hello"), text);
    clearReceived();
    verify(2, postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testDeny() throws Exception {
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(new ProxyAuthResponse(null)))));

    final RemoteNexus remoteNexus = openNexus();
    final String sessionId = generateSessionId();

    final BindFrame bind = new BindFrame(UUID.randomUUID(), 
                                         sessionId,
                                         null,
                                         new String[]{TOPIC},
                                         new String[]{},
                                         null);
    final BindResponseFrame bindRes = remoteNexus.bind(bind).get();
    assertTrue(bindRes.isSuccess());
    verify(0, postRequestedFor(urlMatching(MOCK_PATH)));

    remoteNexus.publish(new PublishTextFrame(TOPIC, "hello"));
    awaitReceived();
    assertNotNull(errors);
    assertNull(text);
    assertNull(binary);
    assertEquals(1, errors.getErrors().length);
    assertEquals(new TopicAccessError("Forbidden", TOPIC), errors.getErrors()[0]);
    clearReceived();
    verify(1, postRequestedFor(urlMatching(MOCK_PATH)));

    remoteNexus.publish(new PublishTextFrame(TOPIC, "hello"));
    awaitReceived();
    assertNotNull(errors);
    assertNull(text);
    assertNull(binary);
    assertEquals(1, errors.getErrors().length);
    assertEquals(new TopicAccessError("Forbidden", TOPIC), errors.getErrors()[0]);
    clearReceived();
    verify(2, postRequestedFor(urlMatching(MOCK_PATH)));
  }

  private static String toJson(Object obj) {
    return new GsonBuilder().disableHtmlEscaping().create().toJson(obj);
  }
}
