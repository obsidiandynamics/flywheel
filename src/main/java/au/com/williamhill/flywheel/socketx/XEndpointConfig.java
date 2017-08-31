package au.com.williamhill.flywheel.socketx;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.socketx.ssl.*;

@Y
public abstract class XEndpointConfig<C extends XEndpointConfig<C>> {
  @YInject
  public long highWaterMark = Long.MAX_VALUE;
  
  @YInject
  public SSLContextProvider sslContextProvider;
  
  public C withHighWaterMark(long highWaterMark) {
    this.highWaterMark = highWaterMark;
    return self();
  }
  
  public C withSSLContextProvider(SSLContextProvider sslContextProvider) {
    this.sslContextProvider = sslContextProvider;
    return self();
  }
  
  @SuppressWarnings("unchecked")
  private C self() {
    return (C) this;
  }
}
