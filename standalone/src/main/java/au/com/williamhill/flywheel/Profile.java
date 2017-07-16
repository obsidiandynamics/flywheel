package au.com.williamhill.flywheel;

import java.io.*;
import java.util.*;

import com.obsidiandynamics.yconf.*;

@Y(Profile.Mapper.class)
public final class Profile {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      final Profile p = new Profile();
      return y
          .when("properties").then(properties -> {
            properties.asMap().entrySet().forEach(e -> {
              final Object value = e.getValue().map(Object.class);
              if (value != null) {
                p.properties.put(e.getKey(), value);
                System.setProperty(e.getKey(), Secret.unmask(value));
              }
            });
          })
          .mapReflectively(p);
    }
  }
  
  public final Map<String, Object> properties = new LinkedHashMap<>();
  
  @YInject
  public Launcher launchers[];
  
  public static Profile fromFile(File file) throws FileNotFoundException, IOException, NoSuchMethodException, SecurityException {
    final Profile profile = new MappingContext()
        .withDomTransform(new ELTransform()
                          .withVariable("env", System.getenv())
                          .withVariable("maxInt", Integer.MAX_VALUE)
                          .withVariable("maxLong", Long.MAX_VALUE)
                          .withFunction("f", "secret", Secret.class.getMethod("of", String.class))
                          .withFunction("f", "notNull", NotNull.class.getMethod("of", Object.class, String.class)))
        .fromReader(new FileReader(file), Profile.class);
    profile.init();
    return profile;
  }
  
  public static long maxLong() {
    return Long.MAX_VALUE;
  }
  
  public static int maxInt() {
    return Integer.MAX_VALUE;
  }
  
  private void init() {
    if (launchers == null) launchers = new Launcher[] {new ConfigLauncher()};
  }
}
