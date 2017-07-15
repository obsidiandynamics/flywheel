package au.com.williamhill.flywheel;

import com.obsidiandynamics.yconf.*;

/**
 *  Represents a value that cannot be null.
 */
@Y
public final class NotNull {
  public static final class NullValueException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    NullValueException(String m) { super(m); }
  }
  
  private final Object value;
  
  private NotNull(@YInject(name="value") Object value, 
                  @YInject(name="error") String error) {
    if (value == null) throw new NullValueException(error);
    this.value = value;
  }
  
  public Object get() {
    return value;
  }
  
  public static NotNull of(Object value, String errorMessage) {
    return new NotNull(value, errorMessage);
  }
  
  @Override
  public String toString() {
    return value.toString();
  }
}
