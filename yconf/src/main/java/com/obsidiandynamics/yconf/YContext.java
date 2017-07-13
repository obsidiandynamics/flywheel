package com.obsidiandynamics.yconf;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.yaml.snakeyaml.*;

public final class YContext {
  private final Map<Class<?>, YMapper> mappers = new HashMap<>();
  
  private Function<Object, Object> domTransform = Function.identity();
  
  public YContext() {
    withMappers(defaultMappers());
  }
  
  private static YMapper getCharMapper() {
    return new YCoercingMapper(Character.class, s -> {
      if (s.length() != 1) throw new YException("Invalid character '" + s + "'", null);
      return s.charAt(0);
    });
  }
  
  private static Map<Class<?>, YMapper> defaultMappers() {
    final Map<Class<?>, YMapper> mappers = new HashMap<>();
    mappers.put(boolean.class, new YCoercingMapper(Boolean.class, Boolean::parseBoolean));
    mappers.put(Boolean.class, new YCoercingMapper(Boolean.class, Boolean::parseBoolean));
    mappers.put(byte.class, new YCoercingMapper(Byte.class, Byte::parseByte));
    mappers.put(Byte.class, new YCoercingMapper(Byte.class, Byte::parseByte));
    mappers.put(char.class, getCharMapper());
    mappers.put(Character.class, getCharMapper());
    mappers.put(double.class, new YCoercingMapper(Double.class, Double::parseDouble));
    mappers.put(Double.class, new YCoercingMapper(Double.class, Double::parseDouble));
    mappers.put(float.class, new YCoercingMapper(Float.class, Float::parseFloat));
    mappers.put(Float.class, new YCoercingMapper(Float.class, Float::parseFloat));
    mappers.put(int.class, new YCoercingMapper(Integer.class, Integer::parseInt));
    mappers.put(Integer.class, new YCoercingMapper(Integer.class, Integer::parseInt));
    mappers.put(long.class, new YCoercingMapper(Long.class, Long::parseLong));
    mappers.put(Long.class, new YCoercingMapper(Long.class, Long::parseLong));
    mappers.put(Object.class, new YRuntimeMapper());
    mappers.put(short.class, new YCoercingMapper(Short.class, Short::parseShort));
    mappers.put(Short.class, new YCoercingMapper(Short.class, Short::parseShort));
    mappers.put(String.class, new YCoercingMapper(String.class, s -> s));
    return mappers;
  }
  
  private YMapper getMapper(Class<?> type) {
    final YMapper existing = mappers.get(type);
    if (existing != null) {
      return cast(existing);
    } else {
      final YMapper newMapper = instantiateMapper(type);
      mappers.put(type, newMapper);
      return newMapper;
    }
  }
  
  Object transformDom(Object dom) {
    return domTransform.apply(dom);
  }
  
  public YContext withDomTransform(Function<Object, Object> domTransform) {
    this.domTransform = domTransform;
    return this;
  }
  
  public YContext withMapper(Class<?> type, YMapper mapper) {
    mappers.put(type, mapper);
    return this;
  }
  
  public YContext withMappers(Map<Class<?>, YMapper> mappers) {
    this.mappers.putAll(mappers);
    return this;
  }
  
  @SuppressWarnings("unchecked")
  static <T> T cast(Object obj) {
    return (T) obj;
  }
  
  private static YMapper instantiateMapper(Class<?> type) {
    final Y y = type.getAnnotation(Y.class);
    if (y == null) throw new YException("No mapper defined for class " + type.getName() + 
                                        "; check that an @" + Y.class.getSimpleName() + " annotation is present", null);
    try {
      return cast(y.value().newInstance());
    } catch (Exception e) {
      throw new YException("Error instantiating mapper " + y.value().getName() + " for type " +
          type.getName(), e);
    }
  }
  
  public <T> T map(Object dom, Class<? extends T> type) {
    if (dom instanceof YObject) throw new IllegalArgumentException("Cannot map an instance of " + YObject.class.getSimpleName());

    if (dom == null) return null;
    final YMapper mapper = getMapper(type != null ? type : Object.class);
    final YObject y = new YObject(dom, this);
    return cast(mapper.map(y));
  }
  
  public <T> T fromStream(InputStream stream, Class<? extends T> type) throws IOException {
    if (stream == null) throw new NullPointerException("Stream is null");
    final Object root;
    try (InputStream input = stream) {
      root = new Yaml().load(input);
    }
    return map(root, type);
  }
  
  public <T> T fromReader(Reader reader, Class<? extends T> type) throws IOException {
    if (reader == null) throw new NullPointerException("Reader is null");
    final Object root;
    try (Reader input = reader) {
      root = new Yaml().load(input);
    }
    return map(root, type);
  }
  
  public <T> T fromString(String yaml, Class<? extends T> type) {
    final Object root = new Yaml().load(yaml);
    return map(root, type);
  }
}
