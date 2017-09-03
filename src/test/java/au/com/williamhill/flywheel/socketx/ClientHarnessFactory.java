package au.com.williamhill.flywheel.socketx;

import au.com.williamhill.flywheel.socketx.util.URIBuilder.*;

@FunctionalInterface
interface ClientHarnessFactory {
  ClientHarness create(Ports ports, boolean https, boolean echo) throws Exception;
}
