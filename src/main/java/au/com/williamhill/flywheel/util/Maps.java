package au.com.williamhill.flywheel.util;

import java.util.*;
import java.util.function.*;

public final class Maps {
  private Maps() {}
  
  /**
   *  Uses double-checked locking to retrieve a value from the map, or to insert a new
   *  value if none exists.
   *  
   *  @param lock The lock object.
   *  @param map The map.
   *  @param key The key.
   *  @param valueFactory A way of creating a value object.
   *  @return The value - either the existing or the created.
   */
  public static <K, V> V putAtomic(Object lock, Map<K, V> map, K key, Supplier<V> valueFactory) {
    final V existing = map.get(key);
    if (existing != null) {
      return existing;
    } else {
      synchronized (lock) {
        final V existing2 = map.get(key);
        if (existing2 != null) {
          return existing2;
        } else {
          final V created = valueFactory.get();
          map.put(key, created);
          return created;
        }
      }
    }
  }
}
