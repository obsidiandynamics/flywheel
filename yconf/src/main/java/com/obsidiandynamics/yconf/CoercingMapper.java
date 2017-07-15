package com.obsidiandynamics.yconf;

import java.util.function.*;

public final class CoercingMapper implements TypeMapper {
  private final Class<?> coercedType;
  
  private final Function<String, ?> converter;

  public <T> CoercingMapper(Class<T> coercedType, Function<String, ? extends T> converter) {
    this.coercedType = coercedType;
    this.converter = converter;
  }

  @Override
  public Object map(YObject y, Class<?> type) {
    if (y.isNull() || y.is(coercedType)) {
      return y.value();
    } else {
      final String str = String.valueOf(y.<Object>value());
      return converter.apply(str);
    }
  }
}
