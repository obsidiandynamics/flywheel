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
  public Object map(YObject y, Class<?> type) {
    final Object val = y.value();
    final String typeVal;
    if (val instanceof Map) {
      final Map<String, Object> map = YContext.cast(val);
      final Object typeV = map.get(typeAttribute);
      if (typeV instanceof String) {
        typeVal = typeFormatter.apply((String) typeV);
      } else {
        typeVal = null;
      }
    } else {
      typeVal = null;
    }

    if (typeVal != null) {
      final Class<?> concreteType;
      try {
        concreteType = Class.forName(typeVal);
      } catch (ClassNotFoundException e) {
        throw new YException("Error loading class", e);
      }
  
      return y.map(concreteType);
    } else {
      return y.value();
    }
  }
}
