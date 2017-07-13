package com.obsidiandynamics.yconf;

import java.util.function.*;

public final class YBasicMapper implements YMapper {
  private final Class<?> type;
  
  private final Function<String, ?> converter;

  public <T> YBasicMapper(Class<T> type, Function<String, ? extends T> converter) {
    this.type = type;
    this.converter = converter;
  }

  @Override
  public Object map(YObject y) {
    if (y.isNull() || y.is(type)) {
      return y.value();
    } else {
      final String str = String.valueOf(y.<Object>value());
      return converter.apply(str);
    }
  }
}
