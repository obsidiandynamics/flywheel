package au.com.williamhill.flywheel.socketx;

import java.util.*;

public class XServerConfig {
  public int port = 8080;
  
  public String path = "/";
  
  public int idleTimeoutMillis = 300_000;
  
  public int pingIntervalMillis = 60_000;
  
  public int scanIntervalMillis = 1_000;
  
  public XMappedServlet[] servlets = new XMappedServlet[0];
  
  public XEndpointConfig endpointConfig = new XEndpointConfig();
  
  public XServerConfig withPort(int port) {
    this.port = port;
    return this;
  }
  
  public XServerConfig withPath(String path) {
    this.path = path;
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
  
  public XServerConfig withServlets(XMappedServlet ... servlets) {
    this.servlets = servlets;
    return this;
  }

  public XServerConfig withEndpointConfig(XEndpointConfig endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  @Override
  public String toString() {
    return "XServerConfig [port=" + port + ", path=" + path + ", idleTimeoutMillis=" + idleTimeoutMillis
           + ", pingIntervalMillis=" + pingIntervalMillis + ", scanIntervalMillis=" + scanIntervalMillis + ", servlets="
           + Arrays.toString(servlets) + ", endpointConfig=" + endpointConfig + "]";
  }
}
