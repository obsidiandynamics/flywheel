package au.com.williamhill.flywheel.edge.auth.httpstub;

import java.net.*;

import com.obsidiandynamics.yconf.*;

@Y
public class HttpStubAuthenticatorConfig {
  @YInject
  URI uri;

  @YInject
  int poolSize = 8;
  
  @YInject
  int timeoutMillis = 60_000;
  
  public HttpStubAuthenticatorConfig withURI(URI uri) {
    this.uri = uri;
    return this;
  }

  public HttpStubAuthenticatorConfig withPoolSize(int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public HttpStubAuthenticatorConfig withTimeoutMillis(int timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
    return this;
  }

  @Override
  public String toString() {
    return "HttpStubAuthenticatorConfig [uri: " + uri + ", poolSize: " + poolSize + ", timeoutMillis: " + timeoutMillis + "]";
  }
}
