package au.com.williamhill.flywheel.edge.auth.httpproxy;

import java.net.*;
import java.util.function.*;

import org.apache.http.client.utils.*;

import com.github.tomakehurst.wiremock.*;
import com.github.tomakehurst.wiremock.junit.*;

final class WireMockURIBuilder {
  private Function<Boolean, Integer> portProvider;
  
  private boolean https;
  
  private String path;
  
  private String host = "localhost";
  
  WireMockURIBuilder withWireMock(WireMockServer wireMock) {
    return withPortProvider(https -> https ? wireMock.httpsPort() : wireMock.port());
  }

  WireMockURIBuilder withWireMock(WireMockRule wireMock) {
    return withPortProvider(https -> https ? wireMock.httpsPort() : wireMock.port());
  }
  
  WireMockURIBuilder withPortProvider(Function<Boolean, Integer> portProvider) {
    this.portProvider = portProvider;
    return this;
  }

  WireMockURIBuilder withHttps(boolean https) {
    this.https = https;
    return this;
  }

  WireMockURIBuilder withPath(String path) {
    this.path = path;
    return this;
  }

  WireMockURIBuilder withHost(String host) {
    this.host = host;
    return this;
  }
  
  URI build() {
    try {
      return new URIBuilder()
          .setScheme(https ? "https" : "http")
          .setHost(host)
          .setPort(portProvider.apply(https))
          .setPath(path)
          .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
