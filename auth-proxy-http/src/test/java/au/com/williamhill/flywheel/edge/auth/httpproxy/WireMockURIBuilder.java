package au.com.williamhill.flywheel.edge.auth.httpproxy;

import java.net.*;

import org.apache.http.client.utils.*;

import com.github.tomakehurst.wiremock.junit.*;

final class WireMockURIBuilder {
  private WireMockRule wireMock;
  
  private boolean https;
  
  private String path;
  
  private String host = "localhost";

  WireMockURIBuilder withWireMock(WireMockRule wireMock) {
    this.wireMock = wireMock;
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
          .setPort(https ? wireMock.httpsPort() : wireMock.port())
          .setPath(path)
          .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
