package au.com.williamhill.flywheel.socketx;

@FunctionalInterface
interface ClientHarnessFactory {
  ClientHarness create(int port, boolean echo) throws Exception;
}
