package com.obsidiandynamics.yconf;

import java.util.*;
import java.util.function.*;

public final class YRuntimeMapper implements YMapper {
  private String typeAttribute = "type";
  
  private Function<String, String> typeFormatter = Function.identity();
  
  public YRuntimeMapper withTypeAttribute(String typeAttribute) {
    this.typeAttribute = typeAttribute;
    return this;
  }
  
  public YRuntimeMapper withTypeFormatter(Function<String, String> typeFormatter) {
    this.typeFormatter = typeFormatter;
    return this;
  }
  
  @Override
  public Object map(YObject y) {
    final Object val = y.value();
    final String type;
    if (val instanceof Map) {
      final Map<String, Object> map = YContext.cast(val);
      final Object typeV = map.get(typeAttribute);
      if (typeV instanceof String) {
        type = typeFormatter.apply((String) typeV);
      } else {
        type = null;
      }
    } else {
      type = null;
    }

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
