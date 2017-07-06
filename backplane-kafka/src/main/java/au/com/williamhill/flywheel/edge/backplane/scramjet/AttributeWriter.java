package au.com.williamhill.flywheel.edge.backplane.scramjet;

import java.util.*;

final class AttributeWriter extends LinkedHashMap<String, Object> {
  private static final long serialVersionUID = 1L;
  
  AttributeWriter(String typeName) {
    put(ScramjetMessage.TYPE_ATT, typeName);
  }
  
  AttributeWriter write(String key, Object value) {
    put(key, value);
    return this;
  }
  
  AttributeWriter writeAll(Map<String, Object> src) {
    putAll(src);
    return this;
  }
}
