package au.com.williamhill.flywheel.socketx;

import com.obsidiandynamics.yconf.*;

@Y
public abstract class XEndpointConfig<C extends XEndpointConfig<C>> {
  @YInject
  public long highWaterMark = Long.MAX_VALUE;
  
  @YInject
  public SSLConfig sslConfig = SSLConfig.getDefault();
  
  public C withHighWaterMark(long highWaterMark) {
    this.highWaterMark = highWaterMark;
    return self();
  }
  
  public C withSSLConfig(SSLConfig sslConfig) {
    this.sslConfig = sslConfig;
    return self();
  }
  
  @SuppressWarnings("unchecked")
  private C self() {
    return (C) this;
  }
}
