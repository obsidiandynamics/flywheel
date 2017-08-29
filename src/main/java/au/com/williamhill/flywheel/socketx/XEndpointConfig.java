package au.com.williamhill.flywheel.socketx;

import com.obsidiandynamics.yconf.*;

@Y
public class XEndpointConfig<C extends XEndpointConfig<C>> {
  @YInject
  public long highWaterMark = Long.MAX_VALUE;
  
  @YInject
  public SSLConfig sslConfig = SSLConfig.getDefault();
  
  @SuppressWarnings("unchecked")
  public C withHighWaterMark(long highWaterMark) {
    this.highWaterMark = highWaterMark;
    return (C) this;
  }
  
  @SuppressWarnings("unchecked")
  public C withSSLConfig(SSLConfig sslConfig) {
    this.sslConfig = sslConfig;
    return (C) this;
  }
}
