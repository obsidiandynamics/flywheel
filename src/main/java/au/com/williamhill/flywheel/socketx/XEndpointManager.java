package au.com.williamhill.flywheel.socketx;

import java.util.*;

public interface XEndpointManager<E extends XEndpoint> {
  Collection<E> getEndpoints();
}
