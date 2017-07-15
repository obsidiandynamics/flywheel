package com.obsidiandynamics.yconf;

import java.util.*;
import java.util.function.*;

/**
 *  A {@link TypeMapper} implementation that relies on an attribute named 'type' in
 *  YAML DOM to indicate the true type of the serialized data, delegating to the
 *  appropriate {@link TypeMapper} for the type hint.<p>
 *  
 *  Note: the name of the attribute can be configured using 
 *  {@link #withTypeAttribute(String)}.
 */
public final class RuntimeMapper implements TypeMapper {
  private String typeAttribute = "type";
  
  private Function<String, String> typeFormatter = Function.identity();
  
  public RuntimeMapper withTypeAttribute(String typeAttribute) {
    this.typeAttribute = typeAttribute;
    return this;
  }
  
  public RuntimeMapper withTypeFormatter(Function<String, String> typeFormatter) {
    this.typeFormatter = typeFormatter;
    return this;
  }
  
  @Override
  public Object map(YObject y, Class<?> type) {
    final Object val = y.value();
    final String typeVal;
    if (val instanceof Map) {
      final Map<String, Object> map = MappingContext.cast(val);
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
        throw new MappingException("Error loading class", e);
      }
  
      return y.map(concreteType);
    } else {
      return y.value();
    }
  }
}
