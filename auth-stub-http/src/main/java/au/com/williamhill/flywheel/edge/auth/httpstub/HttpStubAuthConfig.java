package au.com.williamhill.flywheel.edge.auth.httpstub;

import java.net.*;

import com.obsidiandynamics.yconf.*;

@Y
public class HttpStubAuthConfig {
  @YInject
  URI uri;

  @YInject
  int poolSize = 8;
  
  @YInject
  int timeoutMillis = 60_000;
  
  public HttpStubAuthConfig withURI(URI uri) {
    this.uri = uri;
    return this;
  }

  public HttpStubAuthConfig withPoolSize(int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public HttpStubAuthConfig withTimeoutMillis(int timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
    return this;
  }
}
