package au.com.williamhill.flywheel.yconf;

import java.io.*;
import java.util.*;

import com.obsidiandynamics.yconf.*;

@Y(Profile.Mapper.class)
public final class Profile {
  public static final class Mapper implements YMapper {
    @Override public Object map(YObject y) {
      final Profile p = new Profile();
      y.when("properties").then(properties -> {
        properties.asMap().entrySet().forEach(e -> {
          System.out.println("adding " + e.getValue().value());
          System.out.println("-> " + e.getValue().map(Object.class));
          p.properties.put(e.getKey(), e.getValue().map(String.class));
        });
      });
      return p;
    }
  }
  
  final Map<String, String> properties = new LinkedHashMap<>();
  
  public static Profile fromFile(File file) throws FileNotFoundException, IOException {
    //TODO
    final Map<Object, Object> env = new HashMap<>();
    env.put("buildNo", "8989");
    
    return new YContext()
        .withDomTransform(new ELTransform().withVariable("env", env))
        .fromReader(new FileReader(file), Profile.class);
  }
}
