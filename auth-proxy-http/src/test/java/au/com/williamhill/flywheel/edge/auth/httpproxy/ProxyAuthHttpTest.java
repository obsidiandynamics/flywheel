package au.com.williamhill.flywheel.edge.auth.httpproxy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.concurrent.*;

import org.apache.http.client.utils.*;
import org.apache.http.nio.reactor.*;
import org.awaitility.*;
import org.junit.*;
import org.mockito.*;

import com.github.tomakehurst.wiremock.junit.*;
import com.google.gson.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.Authenticator.*;
import au.com.williamhill.flywheel.frame.*;

public final class ProxyAuthHttpTest {
  private static final String MOCK_PATH = "/auth";
  
  private static final String TOPIC = "test";
  
  @ClassRule
  public static WireMockRule wireMock = new WireMockRule(options()
                                                         .dynamicPort()
                                                         .dynamicHttpsPort());
  private Gson gson;
  private ProxyHttpAuth auth;
  
  @Before
  public void before() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
    gson = new GsonBuilder().disableHtmlEscaping().create();
    auth = new ProxyHttpAuth();
    auth.withUri(getURI(false));
    auth.withPoolSize(4);
    auth.close(); // tests close() before init()
  }
  
  @After
  public void after() throws IOException {
    if (auth != null) auth.close();
    
    auth = null;
  }

  @Test
  public void testAllow() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.init();
    final ProxyAuthResponse expected = new ProxyAuthResponse(1000L);
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(gson.toJson(expected))));
    
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setAuth(new BasicAuth("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(outcome).allow();
    });
    
    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testAllowHttps() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.withUri(getURI(true)).init();
    final ProxyAuthResponse expected = new ProxyAuthResponse(1000L);
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(gson.toJson(expected))));
    
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setAuth(new BasicAuth("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(outcome).allow();
    });
    
    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testDeny() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.init();
    final ProxyAuthResponse expected = new ProxyAuthResponse(0);
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(gson.toJson(expected))));
    
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setAuth(new BasicAuth("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(outcome).forbidden(Mockito.eq(TOPIC));
    });
    
    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testBadStatusCode() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.init();
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(404)));
    
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setAuth(new BasicAuth("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(outcome).forbidden(Mockito.eq(TOPIC));
    });
    
    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testBadEntity() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.init();
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("}{")));
    
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setAuth(new BasicAuth("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(outcome).forbidden(Mockito.eq(TOPIC));
    });
    
    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testTimeout() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth
    .withTimeoutMillis(1)
    .init();
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withFixedDelay(10_000)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));
    
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setAuth(new BasicAuth("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(outcome).forbidden(Mockito.eq(TOPIC));
    });
  }

  private URI getURI(boolean https) throws URISyntaxException {
    return new URIBuilder()
        .setScheme(https ? "https" : "http")
        .setHost("localhost")
        .setPort(https ? wireMock.httpsPort() : wireMock.port())
        .setPath(MOCK_PATH)
        .build();
  }
}
