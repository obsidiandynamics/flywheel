package au.com.williamhill.flywheel.yconfig;

import java.util.*;

@Y(YTyped.Mapper.class)
public interface YTyped {
  static class Mapper implements YMapper<Map<String, Object>, Object> {
    private static final String TYPE_ATT = "type";
    
    @Override
    public Object map(Map<String, Object> yaml, YContext context) {
      final String type = (String) yaml.get(TYPE_ATT);
      if (type == null) {
        throw new YException("No explicit type defined; please set the '" + TYPE_ATT + "' attribute", null);
      }
      
      final Class<?> concreteType;
      try {
        concreteType = Class.forName(type);
      } catch (ClassNotFoundException e) {
        throw new YException("Error loading class", e);
      }
      
      return context.map(yaml, concreteType);
    }
  }
}
