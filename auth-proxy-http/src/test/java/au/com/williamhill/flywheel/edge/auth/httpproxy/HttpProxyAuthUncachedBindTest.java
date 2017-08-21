package au.com.williamhill.flywheel.edge.auth.httpproxy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

import java.net.*;
import java.util.*;

import org.apache.http.client.utils.*;
import org.junit.*;

import com.github.tomakehurst.wiremock.junit.*;
import com.google.gson.*;

import au.com.williamhill.flywheel.edge.auth.*;
import au.com.williamhill.flywheel.edge.auth.NestedAuthenticator.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;

public final class HttpProxyAuthUncachedBindTest extends AbstractAuthTest {
  private static final String MOCK_PATH = "/auth";
  
  private static final String TOPIC = "test";
  
  @ClassRule
  public static WireMockRule wireMock = new WireMockRule(options()
                                                         .dynamicPort()
                                                         .dynamicHttpsPort());
  
  private URI getURI(boolean https) throws URISyntaxException {
    return new URIBuilder()
        .setScheme(https ? "https" : "http")
        .setHost("localhost")
        .setPort(https ? wireMock.httpsPort() : wireMock.port())
        .setPath(MOCK_PATH)
        .build();
  }

  @SuppressWarnings("resource")
  private void setupAuthChains() throws URISyntaxException, Exception {
    setupEdgeNode(new PubAuthChain(), 
                  new SubAuthChain().set(TOPIC, new AuthenticatorWrapper(new HttpProxyAuth()
                                                                         .withUri(getURI(false)))));
  }
  
  @Test
  public void testAllow() throws Exception {
    setupAuthChains();
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
                                         null,
                                         null);
    final BindResponseFrame bindRes = remoteNexus.bind(bind).get();
    assertTrue(bindRes.isSuccess());
    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }
  
  @Test
  public void testDeny() throws Exception {
    setupAuthChains();
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
                                         null,
                                         null);
    final BindResponseFrame bindRes = remoteNexus.bind(bind).get();
    assertFalse(bindRes.isSuccess());
    assertEquals(1, bindRes.getErrors().length);
    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }
  
  private static String toJson(Object obj) {
    return new GsonBuilder().disableHtmlEscaping().create().toJson(obj);
  }
}
