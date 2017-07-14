package au.com.williamhill.flywheel.yconf;

import com.obsidiandynamics.yconf.*;

/**
 *  Obfuscates secrets from general logging operations that typically involve {@link #toString()}. 
 */
@Y(Masked.Mapper.class)
public final class Masked {
  public static final class Mapper implements YMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return new Masked(y.getAttribute("value").map(String.class));
    }
  }
  
  private final String secret;

  Masked(String secret) {
    this.secret = secret;
  }
  
  String unmask() {
    return secret;
  }
  
  public static Masked mask(String secret) {
    return new Masked(secret);
  }
  
  public static String unmask(Object obj) {
    if (obj instanceof Masked) {
      return ((Masked) obj).unmask();
    } else {
      return String.valueOf(obj);
    }
  }
  
  @Override
  public String toString() {
    return "<not shown>";
  }
}
