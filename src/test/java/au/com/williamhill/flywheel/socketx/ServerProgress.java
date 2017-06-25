package au.com.williamhill.flywheel.socketx;

@FunctionalInterface
interface ServerProgress {
  void update(ServerHarness server, long sent);
}
