package au.com.williamhill.flywheel.backplane.scramjet;

import java.util.*;

import com.google.gson.annotations.*;

public final class ScramjetBase64 implements ScramjetPayload {
  public static final String TYPE = "Scramjet.Messages.Base64";
  
  public static final String VALUE_ATT = "value";
  
  @SerializedName(VALUE_ATT)
  private final String value;
  
  public ScramjetBase64(String value) {
    this.value = value;
  }
  
  public ScramjetBase64(byte[] value) {
    this.value = Base64.getEncoder().encodeToString(value);
  }
  
  public String getValue() {
    return value;
  }
  
  public byte[] decode() {
    return Base64.getDecoder().decode(value);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ScramjetBase64 other = (ScramjetBase64) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ScramjetBase64 [value=" + value + "]";
  }

  @Override
  public Object pack() {
    return this;
  }
  
  static ScramjetBase64 unpack(AttributeReader reader) {
    final String value = reader.read(VALUE_ATT);
    return new ScramjetBase64(value);
  }
}