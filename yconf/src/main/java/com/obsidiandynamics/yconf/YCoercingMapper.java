package com.obsidiandynamics.yconf;

import java.util.function.*;

public final class YCoercingMapper implements YMapper {
  private final Class<?> type;
  
  private final Function<String, ?> converter;

  public <T> YCoercingMapper(Class<T> type, Function<String, ? extends T> converter) {
    this.type = type;
    this.converter = converter;
  }

  @Override
  public Object map(YObject y) {
    final Object val = y.value();
    if (val == null) {
      return val;
    } else if (type.isAssignableFrom(val.getClass())) {
      return val;
    } else {
      final String str = String.valueOf(y.<Object>value());
      return converter.apply(str);
    }
  }
}
