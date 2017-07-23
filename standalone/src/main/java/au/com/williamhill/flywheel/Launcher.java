package au.com.williamhill.flywheel;

public interface Launcher extends AutoCloseable {
  void launch(String[] args) throws Exception;
}
