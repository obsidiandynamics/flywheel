package au.com.williamhill.flywheel;

import java.io.*;
import java.net.*;

import com.obsidiandynamics.indigo.util.*;

public final class FlywheelVersion {
  private FlywheelVersion() {}
  
  public static String get() throws IOException {
    return get("flywheel.version") + "_" + get("flywheel.build");
  }
  
  static String get(String versionFile) throws IOException {
    return readResourceHead(versionFile);
  }
  
  private static String readResourceHead(String file) throws IOException {
    final URL url = IndigoVersion.class.getClassLoader().getResource(file);
    if (url == null) throw new FileNotFoundException("resource not found");
    
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
      return reader.readLine().trim();
    }
  }
}
