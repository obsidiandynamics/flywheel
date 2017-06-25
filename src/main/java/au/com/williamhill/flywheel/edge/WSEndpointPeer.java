package au.com.williamhill.flywheel.edge;

import java.net.*;

import au.com.williamhill.flywheel.socketx.*;

final class WSEndpointPeer implements Peer {
  private final XEndpoint endpoint;

  WSEndpointPeer(XEndpoint endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public InetSocketAddress getAddress() {
    return endpoint.getRemoteAddress();
  }

  @Override
  public XEndpoint getEndpoint() {
    return endpoint;
  }

  @Override
  public void close() throws Exception {
    endpoint.close();
  }
  
  @Override
  public String toString() {
    return String.valueOf(getAddress());
  }
}
