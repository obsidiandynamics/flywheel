package au.com.williamhill.flywheel.backplane.scramjet;

import java.util.*;

public final class ScramjetObject implements ScramjetPayload {
  static final String JSON_TYPE_NAME = "Scramjet.Messages.Object";
  
  public final Map<String, Object> atts = new LinkedHashMap<>();
  
  public ScramjetObject put(String attribute, Object value) {
    atts.put(attribute, value);
    return this;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((atts == null) ? 0 : atts.hashCode());
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
    ScramjetObject other = (ScramjetObject) obj;
    if (atts == null) {
      if (other.atts != null)
        return false;
    } else if (!atts.equals(other.atts))
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    return atts.toString();
  }
  
  @Override
  public AttributeWriter pack() {
    return new AttributeWriter(JSON_TYPE_NAME).writeAll(atts);
  }
  
  public Map<String, Object> asMap() {
    return atts;
  }
  
  static ScramjetObject unpack(AttributeReader reader) {
    final ScramjetObject obj = new ScramjetObject();
    obj.atts.putAll(reader.getAttributes());
    obj.atts.remove(ScramjetMessage.TYPE_ATT);
    return obj;
  }
}