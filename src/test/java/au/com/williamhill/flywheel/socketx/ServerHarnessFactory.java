package au.com.williamhill.flywheel.socketx;

@FunctionalInterface
interface ServerHarnessFactory {
  ServerHarness create(int port, ServerProgress progress, int idleTimeout) throws Exception;
}
