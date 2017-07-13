package au.com.williamhill.flywheel.yconf;

import java.io.*;
import java.util.*;

import com.obsidiandynamics.yconf.*;

@Y(Profile.Mapper.class)
public final class Profile {
  public static final class Mapper implements YMapper {
    @Override public Object map(YObject y, Class<?> type) {
      final Profile p = new Profile();
      y.when("properties").then(properties -> {
        properties.asMap().entrySet().forEach(e -> {
          final Object value = e.getValue().map(Object.class);
          if (value == null) throw new IllegalArgumentException("No resolved value for property " + e.getKey());
          p.properties.put(e.getKey(), value);
        });
      });
      return p;
    }
  }
  
  public final Map<String, Object> properties = new LinkedHashMap<>();
  
  public static Profile fromFile(File file) throws FileNotFoundException, IOException {
    return new YContext()
        .withMapper(Object.class, new YRuntimeMapper().withTypeFormatter("au.com.williamhill."::concat))
        .withDomTransform(new ELTransform().withVariable("env", System.getenv()))
        .fromReader(new FileReader(file), Profile.class);
  }
}
