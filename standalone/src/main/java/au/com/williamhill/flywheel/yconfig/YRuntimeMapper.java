package au.com.williamhill.flywheel.yconfig;

import java.util.*;

public final class YRuntimeMapper implements YMapper {
  private String typeAttribute = "type";
  
  public YRuntimeMapper withTypeAttribute(String typeAttribute) {
    this.typeAttribute = typeAttribute;
    return this;
  }
  
  @Override
  public Object map(YObject y) {
    final String type = y.is(Map.class) ? y.getAttribute(typeAttribute).value() : null;

    if (type != null) {
      final Class<?> concreteType;
      try {
        concreteType = Class.forName(type);
      } catch (ClassNotFoundException e) {
        throw new YException("Error loading class", e);
      }
  
      return y.map(concreteType);
    } else {
      return y.value();
    }
  }
}
