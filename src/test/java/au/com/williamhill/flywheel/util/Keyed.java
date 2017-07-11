package au.com.williamhill.flywheel.util;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Thread-safe map of partitions (where a partition can be anything, but is typically a list, queue or 
 *  another map), with a {@link Keyed#forKey(Object)} partition lookup that atomically creates a partition
 *  if one does not already exist, using a factory supplied in the constructor.
 *  
 *  @param <K> The key type.
 *  @param <P> The partition type.
 */
public class Keyed<K, P> {
  protected final Map<K, P> map = new ConcurrentHashMap<>();
  
  private final Supplier<P> partitionFactory;
  
  public Keyed(Supplier<P> partitionFactory) {
    this.partitionFactory = partitionFactory;
  }

  /**
   *  Returns the backing map of keys to partitions.
   *  
   *  @return The backing map.
   */
  public final Map<K, P> asMap() {
    return Collections.unmodifiableMap(map);
  }
  
  /**
   *  Looks up a partition for key, creating one if it doesn't already exist.
   *  
   *  @param key The key.
   *  @return The partition.
   */
  public final P forKey(K key) {
    return getOrSet(map, map, key, partitionFactory);
  }
  
  /**
   *  Utility for atomically retrieving a mapped value if it exists, or assigning a value from a 
   *  given factory if it doesn't.
   *  
   *  @param <K> Key type.
   *  @param <V> Value type.
   *  @param lock The lock object to use.
   *  @param map The map.
   *  @param key The key.
   *  @param valueFactory A way of creating a missing value.
   *  @return The value.
   */
  public static <K, V> V getOrSet(Object lock, Map<K, V> map, K key, Supplier<V> valueFactory) {
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

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [map=" + map + "]";
  }
}
