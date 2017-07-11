package au.com.williamhill.flywheel.edge.backplane.scramjet;

import java.util.*;
import java.util.function.*;

final class AttributeReader {
  private final Map<String, Object> atts;

  AttributeReader(Map<String, Object> atts) {
    this.atts = atts;
  }
  
  Map<String, Object> getAttributes() {
    return atts;
  }
  
  <T> T read(String key) {
    return read(key, () -> null);
  }
  
  <T> T read(String key, Supplier<? extends T> def) {
    @SuppressWarnings("unchecked")
    final T value = (T) atts.get(key);
    return value != null ? value : def.get();
  }
  
  String getTypeName() {
    return (String) atts.get(ScramjetMessage.TYPE_ATT);
  }
}
