package au.com.williamhill.flywheel.edge.auth.httpstub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.awaitility.*;
import org.junit.*;
import org.junit.Test;
import org.mockito.*;

import com.github.tomakehurst.wiremock.*;
import com.github.tomakehurst.wiremock.client.*;
import com.github.tomakehurst.wiremock.junit.*;
import com.google.gson.*;
import com.obsidiandynamics.indigo.benchmark.*;
import com.obsidiandynamics.indigo.benchmark.Summary.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.Authenticator.*;
import au.com.williamhill.flywheel.edge.auth.CachedAuthenticator.*;
import au.com.williamhill.flywheel.edge.auth.httpstub.util.*;
import au.com.williamhill.flywheel.frame.*;
import junit.framework.*;

public final class HttpStubAuthenticatorBenchmark implements TestSupport {
  abstract static class Config implements Spec {
    int n;
    int maxOutstanding;
    int poolSize;
    float warmupFrac;
    URI uri;
    String topic = "topic";
    boolean stats;
    LogConfig log;
    ThrowingRunnable beforeRun = () -> {};
    ThrowingRunnable afterRun = () -> {};
    
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
      return String.format("%,d requests, %.0f%% warmup fraction", n, warmupFrac * 100);
    }
    
    SpecMultiplier assignDefaults() {
      warmupFrac = 0.10f;
      poolSize = 10;
      log = new LogConfig() {{
        summary = stages = LOG;
      }};
      return times(2);
    }

    @Override
    public Summary run() throws Exception {
      return HttpStubAuthenticatorBenchmark.test(this);
    }
  }

  @ClassRule
  public static WireMockRule wireMock = new WireMockRule(options()
                                                         .dynamicPort()
                                                         .dynamicHttpsPort());
  
  private static MappingBuilder postEndpoint(String path) {
    return post(urlEqualTo(path))
        .withHeader("Accept", equalTo("application/json"))
        .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(getJsonResponse()));
  }
  
  private static String getJsonResponse() {
    final StubAuthResponse response = new StubAuthResponse(AuthenticationOutcome.INDEFINITE);
    return new GsonBuilder().disableHtmlEscaping().create().toJson(response);
  }
  
  private static class ResponseCounter {
    final AtomicInteger allowed = new AtomicInteger();
    final AtomicInteger denied = new AtomicInteger();
    
    void reset() {
      allowed.set(0);
      denied.set(0);
    }
    
    int getReceived() {
      return allowed.get() + denied.get();
    }
    
    boolean hasReceived(int messages) {
      return getReceived() >= messages;
    }
  }
  
  private static class MockOutcome implements AuthenticationOutcome {
    private final ResponseCounter counter;
    private final Consumer<MockOutcome> responseCallback;
    private final long started = System.nanoTime();
    
    MockOutcome(ResponseCounter counter, Consumer<MockOutcome> responseCallback) {
      this.counter = counter;
      this.responseCallback = responseCallback;
    }

    @Override
    public void allow(long millis) {
      counter.allowed.incrementAndGet();
      responseCallback.accept(this);
    }

    @Override
    public void deny(TopicAccessError error) {
      counter.denied.incrementAndGet();
      responseCallback.accept(this);
    }
    
    long took() {
      return System.nanoTime() - started;
    }
  }
  
  private static Summary test(Config c) throws Exception {
    c.beforeRun.run();
    final Summary summary = new Summary();
    final long timedStart;
    try (HttpStubAuthenticator auth = new HttpStubAuthenticator(new HttpStubAuthenticatorConfig().withURI(c.uri).withPoolSize(c.poolSize))) {
      auth.attach(Mockito.mock(CachedAuthConnector.class));
      final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
      final int progressInterval = Math.max(1, c.n / 25);
      final ResponseCounter counter = new ResponseCounter();
      
      if (c.log.stages) c.log.out.format("Warming up...\n");
      runSeries(c, nexus, auth, counter, c.warmupMessages, progressInterval, null);
      
      if (c.log.stages) c.log.out.format("Starting timed run...\n");
      timedStart = System.currentTimeMillis();
      runSeries(c, nexus, auth, counter, c.n - c.warmupMessages, progressInterval, summary.stats);
      
      TestCase.assertEquals(0, counter.denied.get());
    } finally {
      c.afterRun.run();
    }
    
    summary.compute(Arrays.asList(new Elapsed() {
      @Override public long getTotalProcessed() {
        return c.n - c.warmupMessages;
      }
      @Override public long getTimeTaken() {
        return System.currentTimeMillis() - timedStart;
      }
    }));
    return summary;
  }
  
  private static void runSeries(Config c, EdgeNexus nexus, HttpStubAuthenticator auth, ResponseCounter counter, 
                                int runs, int progressInterval, Stats stats) {
    counter.reset();
    for (int i = 0; i < runs; i++) {
      final MockOutcome outcome = new MockOutcome(counter, _outcome -> {
        if (c.stats && stats != null) stats.samples.addValue(_outcome.took());
      });
      auth.verify(nexus, c.topic, outcome);
      while (i - counter.getReceived() > c.maxOutstanding) {
        if (c.log.verbose) c.log.out.format("Throttling...");
        TestSupport.sleep(1);
      }
      if (c.log.progress && i % progressInterval == 0) c.log.printProgressBlock();
    }
    Awaitility.dontCatchUncaughtExceptions().await().atMost(60, TimeUnit.SECONDS).until(() -> counter.hasReceived(runs));
  }
  
  @Test
  public void testWithWireMock() throws Exception {
    final boolean useHttps = false;
    final String path = "/auth";
    stubFor(postEndpoint(path));
    
    new Config() {{
      n = 10;
      maxOutstanding = 10;
      uri = new WireMockURIBuilder().withWireMock(wireMock).withPath(path).withHttps(useHttps).build();
      beforeRun = () -> wireMock.resetRequests();
      afterRun = () -> verify(n, postRequestedFor(urlMatching(uri.getPath())));
    }}
    .assignDefaults()
    .test();
  }
  
  @Test
  public void testWithUndertow() throws Exception {
    final boolean useHttps = false;
    final String path = "/auth";
    final UndertowMockServer server = new UndertowMockServer(path, getJsonResponse());
    
    try {
      server.start();
      new Config() {{
        n = 10;
        maxOutstanding = 10;
        uri = new WireMockURIBuilder().withPortProvider(server::getPort).withPath(path).withHttps(useHttps).build();
        beforeRun = () -> server.getRequests().set(0);
        afterRun = () -> TestCase.assertEquals(n, server.getRequests().get());
      }}
      .assignDefaults()
      .test();
    } finally {
      server.stop();
    }
  }
  
  public static final class WireMockStubBenchmark {
    public static void main(String[] args) throws Exception {
      final boolean useHttps = false;
      final String path = "/auth";
  
      final WireMockServer wireMock = new WireMockServer(options()
                                                         .dynamicPort()
                                                         .dynamicHttpsPort());
      try {
        wireMock.start();
        wireMock.stubFor(postEndpoint(path));
        new Config() {{
          n = 20_000;
          maxOutstanding = 100;
          warmupFrac = 0.10f;
          stats = true;
          poolSize = 100;
          uri = new WireMockURIBuilder().withWireMock(wireMock).withPath(path).withHttps(useHttps).build();
          log = new LogConfig() {{
            stages = false;
            progress = intermediateSummaries = true;
            summary = true;
          }};
          beforeRun = () -> wireMock.resetRequests();
          afterRun = () -> wireMock.verify(n, postRequestedFor(urlMatching(uri.getPath())));
        }}
        .testPercentile(1, 5, 50, Summary::byThroughput);
      } finally {
        wireMock.stop();
      }
    }
  } 

  public static final class UndertowStubBenchmark {
    public static void main(String[] args) throws Exception {
      final boolean useHttps = false;
      final String path = "/auth";
  
      final UndertowMockServer server = new UndertowMockServer(path, getJsonResponse());
      
      try {
        server.start();
        new Config() {{
          n = 100_000;
          maxOutstanding = 1000;
          warmupFrac = 0.10f;
          stats = true;
          poolSize = 100;
          uri = new WireMockURIBuilder()
              .withPortProvider(server::getPort).withPath(path).withHttps(useHttps).build();
          log = new LogConfig() {{
            stages = false;
            progress = intermediateSummaries = true;
            summary = true;
          }};
          beforeRun = () -> server.getRequests().set(0);
          afterRun = () -> TestCase.assertEquals(n, server.getRequests().get());
        }}
        .testPercentile(1, 5, 50, Summary::byThroughput);
      } finally {
        server.stop();
      }
    }
  }
}
