package au.com.williamhill.flywheel.edge.auth.httpstub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.*;
import java.net.*;
import java.security.*;

import org.apache.http.nio.reactor.*;
import org.junit.*;
import org.mockito.*;

import com.github.tomakehurst.wiremock.junit.*;
import com.google.gson.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.*;
import au.com.williamhill.flywheel.edge.auth.NestedAuthenticator.*;
import au.com.williamhill.flywheel.edge.auth.httpstub.util.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.util.*;

public final class HttpStubAuthenticatorTest {
  private static final String MOCK_PATH = "/auth";

  private static final String TOPIC = "test";

  @ClassRule
  public static WireMockRule wireMock = new WireMockRule(options()
                                                         .dynamicPort()
                                                         .dynamicHttpsPort());
  private Gson gson;
  private HttpStubAuthenticator auth;

  @Before
  public void before() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
    gson = new GsonBuilder().disableHtmlEscaping().create();
    auth = new HttpStubAuthenticator(new HttpStubAuthenticatorConfig().withURI(getURI(false)).withPoolSize(4));
    auth.close(); // tests close() before init()
  }

  @After
  public void after() throws IOException {
    if (auth != null) auth.close();

    auth = null;
  }
  
  @Test
  public void testRepeatAttachAndClose() throws Exception {
    final AuthConnector connector = Mockito.mock(AuthConnector.class);
    auth.attach(connector);
    auth.attach(connector);
    auth.close();
    auth.close();
  }

  @Test
  public void testAllow() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.attach(Mockito.mock(AuthConnector.class));
    final StubAuthResponse expected = new StubAuthResponse(1000L);
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gson.toJson(expected))));

    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setCredentials(new BasicAuthCredentials("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);

    SocketTestSupport.await().until(() -> {
      Mockito.verify(outcome).allow(Mockito.eq(1000L));
    });

    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testAllowHttps() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.getConfig().withURI(getURI(true));
    auth.attach(Mockito.mock(AuthConnector.class));
    final StubAuthResponse expected = new StubAuthResponse(1000L);
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gson.toJson(expected))));

    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setCredentials(new BasicAuthCredentials("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);

    SocketTestSupport.await().until(() -> {
      Mockito.verify(outcome).allow(1000L);
    });

    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testDeny() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.attach(Mockito.mock(AuthConnector.class));
    final StubAuthResponse expected = new StubAuthResponse(null);
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gson.toJson(expected))));

    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setCredentials(new BasicAuthCredentials("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);

    SocketTestSupport.await().until(() -> {
      Mockito.verify(outcome).forbidden(Mockito.eq(TOPIC));
    });

    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testBadStatusCode() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.attach(Mockito.mock(AuthConnector.class));
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withStatus(404)));

    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setCredentials(new BasicAuthCredentials("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);

    SocketTestSupport.await().until(() -> {
      Mockito.verify(outcome).forbidden(Mockito.eq(TOPIC));
    });

    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testBadEntity() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.attach(Mockito.mock(AuthConnector.class));
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("}{")));

    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setCredentials(new BasicAuthCredentials("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);

    SocketTestSupport.await().until(() -> {
      Mockito.verify(outcome).forbidden(Mockito.eq(TOPIC));
    });

    verify(postRequestedFor(urlMatching(MOCK_PATH)));
  }

  @Test
  public void testTimeout() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    auth.getConfig().withTimeoutMillis(1);
    auth.attach(Mockito.mock(AuthConnector.class));
    stubFor(post(urlEqualTo(MOCK_PATH))
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse()
                        .withFixedDelay(10_000)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    nexus.getSession().setCredentials(new BasicAuthCredentials("user", "pass"));
    final AuthenticationOutcome outcome = Mockito.mock(AuthenticationOutcome.class);
    auth.verify(nexus, TOPIC, outcome);

    SocketTestSupport.await().until(() -> {
      Mockito.verify(outcome).forbidden(Mockito.eq(TOPIC));
    });
  }

  private URI getURI(boolean https) throws URISyntaxException {
    return new WireMockURIBuilder()
        .withWireMock(wireMock)
        .withHttps(https)
        .withPath(MOCK_PATH)
        .build();
  }
}
