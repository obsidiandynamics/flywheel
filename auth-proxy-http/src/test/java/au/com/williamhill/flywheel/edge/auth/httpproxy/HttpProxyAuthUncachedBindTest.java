package au.com.williamhill.flywheel.edge.auth.httpproxy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

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

public final class HttpProxyAuthUncachedBindTest extends AbstractAuthTest {
  private static final boolean USE_HTTPS = true;

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
    setupEdgeNode(new PubAuthChain(), 
                  new SubAuthChain().set(TOPIC, 
                                         new AuthenticatorWrapper(new HttpProxyAuth(new HttpProxyAuthConfig()
                                                                                    .withURI(getURI(USE_HTTPS))))));
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

    final BindFrame bind1 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{TOPIC},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind1Res = remoteNexus.bind(bind1).get();
    assertTrue(bind1Res.isSuccess());
    verify(1, postRequestedFor(urlMatching(MOCK_PATH)));

    final BindFrame unbind1 = new BindFrame(UUID.randomUUID(), 
                                            sessionId,
                                            null,
                                            new String[]{},
                                            new String[]{TOPIC},
                                            null);
    final BindResponseFrame unbind1Res = remoteNexus.bind(unbind1).get();
    assertTrue(unbind1Res.isSuccess());
    verify(1, postRequestedFor(urlMatching(MOCK_PATH)));

    final BindFrame bind2 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{TOPIC},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind2Res = remoteNexus.bind(bind2).get();
    assertTrue(bind2Res.isSuccess());
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
    assertFalse(bindRes.isSuccess());
    assertEquals(1, bindRes.getErrors().length);
    verify(1, postRequestedFor(urlMatching(MOCK_PATH)));
  }

  private static String toJson(Object obj) {
    return new GsonBuilder().disableHtmlEscaping().create().toJson(obj);
  }
}
