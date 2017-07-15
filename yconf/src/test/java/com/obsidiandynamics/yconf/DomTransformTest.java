package com.obsidiandynamics.yconf;

import static junit.framework.TestCase.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.junit.*;

public final class DomTransformTest {
  private static final Function<Object, Object> STRING_CAPITALIZER = dom -> {
    if (dom instanceof String) {
      return ((String) dom).toUpperCase();
    } else {
      return dom;
    }
  };

  @Test
  public void testLowerToUpper() {
    final String s = new MappingContext().withDomTransform(STRING_CAPITALIZER).map("hello", String.class);
    assertEquals("HELLO", s);
  }

  @Test
  public void testNull() {
    final String s = new MappingContext().withDomTransform(STRING_CAPITALIZER).map(null, String.class);
    assertNull(s);
  }

  @Test
  public void testList() {
    final List<?> out = new MappingContext()
        .withDomTransform(STRING_CAPITALIZER)
        .withMapper(List.class, (y, type) -> y.asList().stream()
                    .map(i -> i.map(String.class)).collect(Collectors.toList()))
        .map(Arrays.asList("foo", "bar"), List.class);
    assertEquals(Arrays.asList("FOO", "BAR"), out);
  }

  @Test
  public void testMap() {
    final Map<Object, Object> map = new HashMap<>();
    map.put("f", "foo");
    map.put("b", "bar");
    
    class Tuple<K, V> {
      final K k;
      final V v;
      
      Tuple(K k, V v) {
        this.k = k;
        this.v = v;
      }
    }
    
    final Map<?, ?> out = new MappingContext()
        .withDomTransform(STRING_CAPITALIZER)
        .withMapper(Map.class, (y, type) -> y.asMap().entrySet().stream()
                    .map(e -> new Tuple<>(e.getKey(), e.getValue().map(String.class)))
                    .collect(Collectors.toMap(t -> t.k, t -> t.v)))
        .map(map, Map.class);
    
    final Map<Object, Object> upperMap = new HashMap<>();
    upperMap.put("f", "FOO");
    upperMap.put("b", "BAR");
    
    assertEquals(upperMap, out);
  }
}
