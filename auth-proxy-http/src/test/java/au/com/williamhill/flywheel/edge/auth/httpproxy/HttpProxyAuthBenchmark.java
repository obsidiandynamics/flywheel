package au.com.williamhill.flywheel.edge.auth.httpproxy;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;

import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.http.client.utils.*;
import org.awaitility.*;
import org.junit.*;
import org.mockito.*;

import com.github.tomakehurst.wiremock.junit.*;
import com.obsidiandynamics.indigo.benchmark.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.*;
import au.com.williamhill.flywheel.edge.auth.NestedAuthenticator.*;
import au.com.williamhill.flywheel.frame.*;

public final class HttpProxyAuthBenchmark implements TestSupport {
  abstract static class Config implements Spec {
    int port;
    int n;
    int maxOutstanding;
    float warmupFrac;
    boolean useHttps;
    String host;
    String path;
    String topic;
    LogConfig log;
    
    /* Derived fields. */
    int warmupMessages;
    
    @Override
    public void init() {
      warmupMessages = (int) (n * warmupFrac);
    }

    @Override
    public LogConfig getLog() {
      return log;
    }

    @Override
    public String describe() {
      return String.format("%,d requests, %.0f%% warmup fraction",
                           n, warmupFrac * 100);
    }
    
    Config withWireMock(WireMockRule wireMock, boolean useHttps) {
      this.useHttps = useHttps;
      port = useHttps ? wireMock.httpsPort() : wireMock.port();
      return this;
    }
    
    SpecMultiplier assignDefaults() {
      warmupFrac = 0.10f;
      host = "localhost";
      path = "/auth";
      topic = "topic";
      log = new LogConfig() {{
        summary = stages = LOG;
      }};
      return times(2);
    }
    
    URI getURI() {
      try {
        return new URIBuilder()
            .setScheme(useHttps ? "https" : "http")
            .setHost(host)
            .setPort(port)
            .setPath(path)
            .build();
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Summary run() throws Exception {
      return HttpProxyAuthBenchmark.test(this);
    }
  }

  @ClassRule
  public static WireMockRule wireMock = new WireMockRule(options()
                                                         .dynamicPort()
                                                         .dynamicHttpsPort());

  @Test
  public void test() throws Exception {
    final boolean useHttps = false;
    new Config() {{
      n = 100;
      maxOutstanding = 10;
    }}
    .withWireMock(wireMock, useHttps)
    .assignDefaults()
    .test();
  }
  
  private static class MockOutcome implements AuthenticationOutcome {
    private final AtomicInteger allowed = new AtomicInteger();
    private final AtomicInteger denied = new AtomicInteger();

    @Override
    public void allow(long millis) {
      allowed.incrementAndGet();
    }

    @Override
    public void deny(TopicAccessError error) {
      denied.incrementAndGet();
    }
    
//    received
  }
  
  private static Summary test(Config c) throws Exception {
    try (HttpProxyAuth auth = new HttpProxyAuth(new HttpProxyAuthConfig().withURI(c.getURI()))) {
      auth.attach(Mockito.mock(AuthConnector.class));
      final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
      final MockOutcome mockOutcome = new MockOutcome();
      for (int i = 0; i < c.n; i++) {
        auth.verify(nexus, c.topic, mockOutcome);
        while (i - mockOutcome.allowed.get() > c.maxOutstanding) {
          TestSupport.sleep(1);
        }
      }
//      Awaitility.dontCatchUncaughtExceptions().await().atMost(60, TimeUnit.SECONDS).until(() -> 
      return null;//TODO.class)
    }
  }
}
