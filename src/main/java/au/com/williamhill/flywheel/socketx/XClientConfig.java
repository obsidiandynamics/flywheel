package au.com.williamhill.flywheel.socketx;

public class XClientConfig {
  public int idleTimeoutMillis = 300_000;
  
  public int scanIntervalMillis = 1_000;
  
  public XEndpointConfig endpointConfig = new XEndpointConfig();
  
  public boolean hasIdleTimeout() {
    return idleTimeoutMillis != 0;
  }
  
  public XClientConfig withIdleTimeout(int idleTimeoutMillis) {
    this.idleTimeoutMillis = idleTimeoutMillis;
    return this;
  }
  
  public XClientConfig withScanInterval(int scanIntervalMillis) {
    this.scanIntervalMillis = scanIntervalMillis;
    return this;
  }

  public XClientConfig withEndpointConfig(XEndpointConfig endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }
}
