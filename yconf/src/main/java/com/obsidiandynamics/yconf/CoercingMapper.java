package com.obsidiandynamics.yconf;

import java.util.function.*;

/**
 *  A {@link TypeMapper} implementation that attempts to coerce the underlying
 *  DOM to a target type by first serializing it to a {@link String}, then
 *  deserializing it back to desired type using the supplied {@code converter}
 *  {@link Function}.<p>
 *  
 *  Note: if the DOM is already of the target type, this mapper acts as a pass-through.
 */
public final class CoercingMapper implements TypeMapper {
  @FunctionalInterface
  public interface StringConverter<T> {
    T convert(String str) throws Throwable;
  }
  
  private final Class<?> coercedType;
  
  private final StringConverter<?> converter;

  public <T> CoercingMapper(Class<T> coercedType, StringConverter<? extends T> converter) {
    this.coercedType = coercedType;
    this.converter = converter;
  }

  @Override
  public Object map(YObject y, Class<?> type) {
    if (y.is(coercedType)) {
      return y.value();
    } else {
      final String str = String.valueOf(y.<Object>value());
      try {
        return converter.convert(str);
      } catch (Throwable e) {
        throw new MappingException(null, e);
      }
    }
  }
}
