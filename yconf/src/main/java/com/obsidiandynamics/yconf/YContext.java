package com.obsidiandynamics.yconf;

import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

public final class YContext {
  private final Map<Class<?>, YMapper> mappers = new HashMap<>();
  
  public YContext() {
    withMappers(defaultMappers());
  }
  
  private static Map<Class<?>, YMapper> defaultMappers() {
    final Map<Class<?>, YMapper> mappers = new HashMap<>();
    mappers.put(Boolean.class, new YBasicMapper(Boolean.class, Boolean::parseBoolean));
    mappers.put(Byte.class, new YBasicMapper(Byte.class, Byte::parseByte));
    mappers.put(Character.class, new YBasicMapper(Character.class, s -> {
      if (s.length() != 1) throw new YException("Invalid character '" + s + "'", null);
      return s.charAt(0);
    }));
    mappers.put(Double.class, new YBasicMapper(Double.class, Double::parseDouble));
    mappers.put(Float.class, new YBasicMapper(Float.class, Float::parseFloat));
    mappers.put(Integer.class, new YBasicMapper(Integer.class, Integer::parseInt));
    mappers.put(Long.class, new YBasicMapper(Long.class, Long::parseLong));
    mappers.put(Object.class, new YRuntimeMapper());
    mappers.put(Short.class, new YBasicMapper(Short.class, Short::parseShort));
    mappers.put(String.class, new YBasicMapper(String.class, s -> s));
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
    
    final YMapper mapper = getMapper(type != null ? type : Object.class);
    final YObject y = new YObject(dom, this);
    if (y.isNull()) return null;
    else return cast(mapper.map(y));
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
