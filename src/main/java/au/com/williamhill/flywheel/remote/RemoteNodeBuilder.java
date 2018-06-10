package au.com.williamhill.flywheel.remote;

import com.obsidiandynamics.socketx.*;

import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.frame.Wire.*;

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
      clientFactory = (XClientFactory<?>) 
          Class.forName("com.obsidiandynamics.socketx.undertow.UndertowClient$Factory").getConstructor().newInstance();
    }
  }
  
  public RemoteNode build() throws Exception {
    init();
    return new RemoteNode(clientFactory, clientConfig, wire);
  }
}