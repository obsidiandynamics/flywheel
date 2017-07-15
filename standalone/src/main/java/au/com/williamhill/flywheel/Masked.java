package au.com.williamhill.flywheel;

import com.obsidiandynamics.yconf.*;

/**
 *  Obfuscates secrets from general logging operations that typically involve {@link #toString()}. 
 */
@Y
public final class Masked {
  private final String secret;

  Masked(@YInject(name="secret") String secret) {
    this.secret = secret;
  }
  
  String unmask() {
    return secret;
  }
  
  public static Masked mask(String secret) {
    return new Masked(secret);
  }
  
  public static String unmask(Object obj) {
    if (obj == null) {
      return null;
    } else if (obj instanceof Masked) {
      return ((Masked) obj).unmask();
    } else {
      return String.valueOf(obj);
    }
  }
  
  @Override
  public String toString() {
    return "<masked>";
  }
}
