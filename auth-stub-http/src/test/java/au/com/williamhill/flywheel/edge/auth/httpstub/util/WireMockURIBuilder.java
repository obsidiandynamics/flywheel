package au.com.williamhill.flywheel.edge.auth.httpstub.util;

import com.github.tomakehurst.wiremock.*;
import com.github.tomakehurst.wiremock.junit.*;
import com.obsidiandynamics.socketx.util.*;

public final class WireMockURIBuilder extends URIBuilder<WireMockURIBuilder> {
  public WireMockURIBuilder withWireMock(WireMockServer wireMock) {
    return withPortProvider(https -> https ? wireMock.httpsPort() : wireMock.port());
  }

  public WireMockURIBuilder withWireMock(WireMockRule wireMock) {
    return withPortProvider(https -> https ? wireMock.httpsPort() : wireMock.port());
  }
}
