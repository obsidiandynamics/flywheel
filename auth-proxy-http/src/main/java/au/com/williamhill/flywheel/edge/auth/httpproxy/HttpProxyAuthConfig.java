package au.com.williamhill.flywheel.edge.auth.httpproxy;

import java.net.*;

import com.obsidiandynamics.yconf.*;

@Y
public class HttpProxyAuthConfig {
  @YInject
  URI uri;

  @YInject
  int poolSize = 8;
  
  @YInject
  int timeoutMillis = 60_000;
  
  public HttpProxyAuthConfig withURI(URI uri) {
    this.uri = uri;
    return this;
  }

  public HttpProxyAuthConfig withPoolSize(int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public HttpProxyAuthConfig withTimeoutMillis(int timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
    return this;
  }
}
