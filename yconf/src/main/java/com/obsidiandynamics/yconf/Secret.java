package com.obsidiandynamics.yconf;

/**
 *  Obfuscates secrets from general logging operations that typically 
 *  involve {@link #toString()}. 
 */
@Y
public final class Secret {
  private final String value;

  private Secret(@YInject(name="value") String value) {
    this.value = value;
  }
  
  String unmask() {
    return value;
  }
  
  public static Secret of(String secret) {
    return new Secret(secret);
  }
  
  public static String unmask(Object obj) {
    if (obj == null) {
      return null;
    } else if (obj instanceof Secret) {
      return ((Secret) obj).unmask();
    } else {
      return String.valueOf(obj);
    }
  }
  
  @Override
  public String toString() {
    return "<masked>";
  }
}
