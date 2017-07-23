package au.com.williamhill.flywheel;

import com.obsidiandynamics.yconf.*;

@Y
public final class TestLauncher implements Launcher {
  boolean launched;
  
  boolean closed;
  
  @Override
  public void launch(String[] args) throws Exception {
    launched = true;
  }

  @Override
  public void close() throws Exception {
    closed = true;
  }
}
