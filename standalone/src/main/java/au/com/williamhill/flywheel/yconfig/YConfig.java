package au.com.williamhill.flywheel.yconfig;

import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

public final class YConfig {
  private YConfig() {}
  
  @SuppressWarnings("unchecked")
  public static Map<String, Object> fromFile(File file) throws FileNotFoundException, IOException {
    final Yaml yaml = new Yaml();
    try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      return yaml.loadAs(in, Map.class);
    }
  }
}
