package au.com.williamhill.flywheel.socketx;

public class XServerConfig {
  public int port = 6667;
  
  public String contextPath = "/";
  
  public int idleTimeoutMillis = 300_000;
  
  public int pingIntervalMillis = 60_000;
  
  public int scanIntervalMillis = 1_000;
  
  public XEndpointConfig endpointConfig = new XEndpointConfig();
  
  public XServerConfig withPort(int port) {
    this.port = port;
    return this;
  }
  
  public XServerConfig withContextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }
  
  public XServerConfig withIdleTimeout(int idleTimeoutMillis) {
    this.idleTimeoutMillis = idleTimeoutMillis;
    return this;
  }
  
  public XServerConfig withPingInterval(int pingIntervalMillis) {
    this.pingIntervalMillis = pingIntervalMillis;
    return this;
  }
  
  public XServerConfig withScanInterval(int scanIntervalMillis) {
    this.scanIntervalMillis = scanIntervalMillis;
    return this;
  }

  public XServerConfig withEndpointConfig(XEndpointConfig endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }
}
