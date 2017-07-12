package au.com.williamhill.flywheel.yconfig;

@Y(YRuntimeTyped.Mapper.class)
public interface YRuntimeTyped {
  static final class Mapper implements YMapper {
    @Override
    public Object map(YObject y) {
      final String runtimeTypeAttribute = y.getContext().getRuntimeTypeAttribute();
      final String type = y.getAttribute(runtimeTypeAttribute).value();
      if (type == null) {
        throw new YException("No explicit type defined; set the '" + runtimeTypeAttribute + 
                             "' attribute", null);
      }
      
      final Class<?> concreteType;
      try {
        concreteType = Class.forName(type);
      } catch (ClassNotFoundException e) {
        throw new YException("Error loading class", e);
      }
      
      return y.map(concreteType);
    }
  }
}
