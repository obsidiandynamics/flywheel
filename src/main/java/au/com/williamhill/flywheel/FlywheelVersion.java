package au.com.williamhill.flywheel;

import java.io.*;

import com.obsidiandynamics.version.*;

public final class FlywheelVersion {
  private FlywheelVersion() {}
  
  public static String get() throws IOException {
    return AppVersion.get("flywheel");
  }
}
