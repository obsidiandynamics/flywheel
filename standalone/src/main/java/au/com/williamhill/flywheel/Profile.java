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
    return new MappingContext()
        .withDomTransform(new JuelTransform()
                          .withFunction("randomUUID", UUID.class.getMethod("randomUUID"))
                          .withVariable("maxInt", Integer.MAX_VALUE)
                          .withVariable("maxLong", Long.MAX_VALUE))
        .withParser(new SnakeyamlParser())
        .fromReader(new FileReader(file))
        .map(Profile.class);
  }
}
