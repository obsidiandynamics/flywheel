package au.com.williamhill.flywheel.util;

import java.util.*;
import java.util.function.*;

import org.slf4j.*;

public final class ConditionalMDC {
  private final Map<String, Object> map;
  
  private ConditionalMDC(Map<String, Object> map) {
    this.map = map;
  }

  public static ConditionalMDC on(BooleanSupplier condition) {
     return condition.getAsBoolean() ? new ConditionalMDC(new LinkedHashMap<>()) : new ConditionalMDC(null);
  }
  
  public ConditionalMDC put(String key, Object value) {
    if (map != null) {
      map.put(key, value);
    }
    return this;
  }
  
  public void assign() {
    if (map != null) {
      final String fieldNames = getDelimitedFieldNames();
      MDC.put("root", fieldNames);
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        MDC.put(entry.getKey(), String.valueOf(entry.getValue()));
      }
    }
  }
  
  private String getDelimitedFieldNames() {
    final StringBuilder sb = new StringBuilder();
    for (Iterator<String> keyIt = map.keySet().iterator();;) {
      final String key = keyIt.next();
      sb.append(key);
      if (keyIt.hasNext()) {
        sb.append(',');
      } else {
        break;
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return "ConditionalMDC [map=" + map + "]";
  }
}