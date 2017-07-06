package au.com.williamhill.flywheel.backplane.scramjet;

import java.util.*;

public final class ScramjetObject implements ScramjetPayload {
  static final String JSON_TYPE_NAME = "Scramjet.Messages.Object";
  public final Map<String, Object> atts = new LinkedHashMap<>();
  
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
}