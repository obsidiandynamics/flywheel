package au.com.williamhill.flywheel.edge;

import java.net.*;

import au.com.williamhill.flywheel.socketx.*;

public interface Peer extends AutoCloseable {
  InetSocketAddress getAddress();
  
  XEndpoint getEndpoint();
  
  default boolean hasEndpoint() {
    return getEndpoint() != null;
  }
}
