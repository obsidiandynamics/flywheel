package au.com.williamhill.flywheel.socketx;

import java.util.*;

public interface XEndpointManager<E extends XEndpoint> {
  Collection<E> getEndpoints();
  
  default void closeEndpoints(int waitMillis) throws Exception {
    final Collection<E> endpoints = getEndpoints();
    for (E endpoint : endpoints) {
      endpoint.close();
    }
    for (E endpoint : endpoints) {
      endpoint.awaitClose(waitMillis);
    }
  }
}
