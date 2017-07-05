package au.com.williamhill.flywheel.remote;

import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.frame.Wire.*;
import au.com.williamhill.flywheel.socketx.*;

public final class RemoteNodeBuilder {
  private XClientFactory<?> clientFactory;
  private XClientConfig clientConfig = new XClientConfig();
  private Wire wire = new Wire(false, LocationHint.REMOTE);
  
  public RemoteNodeBuilder withClientFactory(XClientFactory<?> clientFactory) {
    this.clientFactory = clientFactory;
    return this;
  }
  
  public RemoteNodeBuilder withClientConfig(XClientConfig clientConfig) {
    this.clientConfig = clientConfig;
    return this;
  }
  
  public RemoteNodeBuilder withWire(Wire wire) {
    this.wire = wire;
    return this;
  }
  
  private void init() throws Exception {
    if (clientFactory == null) {
      clientFactory = (XClientFactory<?>) Class.forName("au.com.williamhill.flywheel.socketx.undertow.UndertowClient$Factory").newInstance();
    }
  }
  
  public RemoteNode build() throws Exception {
    init();
    return new RemoteNode(clientFactory, clientConfig, wire);
  }
}