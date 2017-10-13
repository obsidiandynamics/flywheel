package au.com.williamhill.flywheel;

import java.io.*;
import java.net.*;

import com.obsidiandynamics.indigo.util.*;

//TODO use fulcrum
public final class FlywheelVersion {
  interface DefaultValueSupplier {
    String get() throws IOException;
  }
  
  static final class Constant implements DefaultValueSupplier {
    private final String def;
    
    Constant(String def) { this.def = def; }

    @Override
    public String get() throws FileNotFoundException {
      return def;
    }
  }
  
  private FlywheelVersion() {}
  
  public static String get() throws IOException {
    return get("flywheel.version") + "_" + get("flywheel.build", new Constant("0"));
  }
    
  static String get(String versionFile) throws IOException {
    return get(versionFile, () -> {
      throw new FileNotFoundException("Not found: " + versionFile);
    });
  }
  
  static String get(String versionFile, DefaultValueSupplier defaultValueSupplier) throws IOException {
    return readResourceHead(versionFile, defaultValueSupplier);
  }
  
  private static String readResourceHead(String file, DefaultValueSupplier defaultValueSupplier) throws IOException {
    final URL url = IndigoVersion.class.getClassLoader().getResource(file);
    if (url == null) return defaultValueSupplier.get();
    
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
      return reader.readLine().trim();
    }
  }
}
