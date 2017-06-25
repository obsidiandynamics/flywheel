package au.com.williamhill.flywheel.edge;

import java.net.*;

import au.com.williamhill.flywheel.socketx.*;

final class LocalPeer implements Peer {
  private static final LocalPeer INSTANCE = new LocalPeer();
  
  static LocalPeer instance() { return INSTANCE; }
  
  @Override
  public InetSocketAddress getAddress() {
    return null;
  }

  @Override
  public XEndpoint getEndpoint() {
    return null;
  }
  
  @Override
  public void close() {}

  @Override
  public String toString() {
    return "local";
  }
}
