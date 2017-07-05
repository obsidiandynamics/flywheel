package au.com.williamhill.flywheel.edge;

import au.com.williamhill.flywheel.edge.auth.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.frame.Wire.*;
import au.com.williamhill.flywheel.socketx.*;

public final class EdgeNodeBuilder {
  private XServerFactory<?> serverFactory;
  private XServerConfig serverConfig = new XServerConfig();
  private Wire wire = new Wire(false, LocationHint.EDGE);
  private Interchange interchange;
  private AuthChain pubAuthChain = AuthChain.createPubDefault();
  private AuthChain subAuthChain = AuthChain.createSubDefault();
  private Backplane backplane = new NoOpBackplane();
  
  private void init() throws Exception {
    if (serverFactory == null) {
      serverFactory = (XServerFactory<?>) Class.forName("au.com.williamhill.flywheel.socketx.undertow.UndertowServer$Factory").newInstance();
    }
    
    if (interchange == null) {
      interchange = new RoutingInterchange();
    }
  }
  
  public EdgeNodeBuilder withServerFactory(XServerFactory<?> serverFactory) {
    this.serverFactory = serverFactory;
    return this;
  }
  
  public EdgeNodeBuilder withServerConfig(XServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    return this;
  }
  
  public EdgeNodeBuilder withWire(Wire wire) {
    this.wire = wire;
    return this;
  }

  public EdgeNodeBuilder withInterchange(Interchange interchange) {
    this.interchange = interchange;
    return this;
  }
  
  public EdgeNodeBuilder withPubAuthChain(AuthChain pubAuthChain) {
    this.pubAuthChain = pubAuthChain;
    return this;
  }
  
  public EdgeNodeBuilder withSubAuthChain(AuthChain subAuthChain) {
    this.subAuthChain = subAuthChain;
    return this;
  }
  
  public EdgeNodeBuilder withBackplane(Backplane backplane) {
    this.backplane = backplane;
    return this;
  }

  public EdgeNode build() throws Exception {
    init();
    return new EdgeNode(serverFactory, serverConfig, wire, interchange, pubAuthChain, subAuthChain, backplane);
  }
}