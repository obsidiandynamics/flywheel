package au.com.williamhill.flywheel.edge;

public interface Plugin extends AutoCloseable {
  void onBuild(EdgeNodeBuilder builder) throws Exception;
  
  void onRun(EdgeNode edge) throws Exception;
}
