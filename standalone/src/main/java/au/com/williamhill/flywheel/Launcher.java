package au.com.williamhill.flywheel;

@FunctionalInterface
public interface Launcher {
  void launch(String[] args, Profile profile) throws Exception;
}
