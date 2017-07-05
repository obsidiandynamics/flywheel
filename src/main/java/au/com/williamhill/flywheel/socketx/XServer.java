package au.com.williamhill.flywheel.socketx;

public interface XServer<E extends XEndpoint> extends AutoCloseable {
  XEndpointManager<E> getEndpointManager();
  
  XServerConfig getConfig();
}
