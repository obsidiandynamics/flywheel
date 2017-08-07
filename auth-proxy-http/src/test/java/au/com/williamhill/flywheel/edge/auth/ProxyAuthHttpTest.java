package au.com.williamhill.flywheel.edge.auth;

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
  
  @Rule
  public WireMockRule wireMock = new WireMockRule(options()
                                                  .dynamicPort()
                                                  .dynamicHttpsPort());
  private Gson gson;
  private ProxyHttpAuth auth;
  
  @Before
  public void before() throws URISyntaxException, KeyManagementException, IOReactorException, NoSuchAlgorithmException, KeyStoreException {
    gson = new GsonBuilder().disableHtmlEscaping().create();
    auth = new ProxyHttpAuth();
    auth.withUri(getURI());
    auth.init();
  }
  
  @After
  public void after() throws IOException {
    if (auth != null) auth.close();
    
    auth = null;
  }

  @Test
  public void testValid() throws URISyntaxException {
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
    auth.verify(nexus, "test", outcome);
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Mockito.verify(outcome).allow();
    });
  }

  private URI getURI() throws URISyntaxException {
    return new URIBuilder()
        .setScheme("http")
        .setHost("localhost")
        .setPort(wireMock.port())
        .setPath(MOCK_PATH)
        .build();
  }
}
