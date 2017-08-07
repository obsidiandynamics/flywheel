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
  private AuthChain<PubAuthChain> pubAuthChain = new PubAuthChain();
  private AuthChain<SubAuthChain> subAuthChain = new SubAuthChain();
  private Backplane backplane = new NoOpBackplane();
  private Plugin[] plugins = new Plugin[0];
  
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
  
  public EdgeNodeBuilder withPubAuthChain(AuthChain<PubAuthChain> pubAuthChain) {
    this.pubAuthChain = pubAuthChain;
    return this;
  }
  
  public EdgeNodeBuilder withSubAuthChain(AuthChain<SubAuthChain> subAuthChain) {
    this.subAuthChain = subAuthChain;
    return this;
  }
  
  public EdgeNodeBuilder withBackplane(Backplane backplane) {
    this.backplane = backplane;
    return this;
  }
  
  public EdgeNodeBuilder withPlugins(Plugin... plugins) {
    this.plugins = plugins;
    return this;
  }

  public EdgeNode build() throws Exception {
    init();
    for (Plugin plugin : plugins) {
      plugin.onBuild(this);
    }
    return new EdgeNode(serverFactory, serverConfig, wire, interchange, 
                        pubAuthChain, subAuthChain, backplane, plugins);
  }
}