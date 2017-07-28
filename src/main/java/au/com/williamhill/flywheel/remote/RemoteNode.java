package au.com.williamhill.flywheel.remote;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.socketx.*;

public final class RemoteNode implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(RemoteNode.class);
  
  private final XClient<?> client;
  
  private final Wire wire;
  
  private final List<RemoteNexus> nexuses = new CopyOnWriteArrayList<>();
  
  public RemoteNode(XClientFactory<?> clientFactory, XClientConfig config, Wire wire) throws Exception {
    this.client = clientFactory.create(config);
    this.wire = wire;
  }
  
  public RemoteNexus open(URI uri, RemoteNexusHandler handler) throws Exception {
    if (LOG.isDebugEnabled()) LOG.debug("Connecting to {}", uri);
    final RemoteNexus nexus = new RemoteNexus(RemoteNode.this);
    final EndpointAdapter<XEndpoint> adapter = new EndpointAdapter<>(RemoteNode.this, nexus, handler);
    client.connect(uri, adapter);
    nexuses.add(nexus);
    return nexus;
  }
  
  void addNexus(RemoteNexus nexus) {
    nexuses.add(nexus);
  }
  
  void removeNexus(RemoteNexus nexus) {
    nexuses.remove(nexus);
  }
  
  /**
   *  Obtains the currently connected nexuses.
   *  
   *  @return List of nexuses.
   */
  public List<RemoteNexus> getNexuses() {
    return Collections.unmodifiableList(new ArrayList<>(nexuses));
  }
  
  Wire getWire() {
    return wire;
  }

  @Override
  public void close() throws Exception {
    client.close();
  }
  
  public static RemoteNodeBuilder builder() {
    return new RemoteNodeBuilder();
  }
}
