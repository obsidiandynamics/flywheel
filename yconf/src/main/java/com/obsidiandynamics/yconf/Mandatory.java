package com.obsidiandynamics.yconf;

/**
 *  Represents a value that cannot be null.
 */
@Y(Mandatory.Mapper.class)
public final class Mandatory {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return of(y.mapAttribute("value", Object.class), y.mapAttribute("error", String.class));
    }
  }
  
  public static final class MissingValueException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    MissingValueException(String m) { super(m); }
  }
  
  private Mandatory() {}
  
  public static Object of(Object value, String errorMessage) {
    if (value == null) throw new MissingValueException(errorMessage);
    return value;
  }
}
