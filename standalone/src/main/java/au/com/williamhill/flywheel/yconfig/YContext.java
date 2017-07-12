package au.com.williamhill.flywheel.yconfig;

import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

public final class YContext {
  private final Map<Class<?>, YMapper> mappers = new HashMap<>();
  
  private String runtimeTypeAttribute = "type";
  
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
  
  String getRuntimeTypeAttribute() {
    return runtimeTypeAttribute;
  }
  
  public YContext withMapper(Class<?> type, YMapper mapper) {
    mappers.put(type, mapper);
    return this;
  }
  
  public YContext withRuntimeTypeAttribute(String runtimeTypeAttribute) {
    this.runtimeTypeAttribute = runtimeTypeAttribute;
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
  
  public <T> T map(Object yaml, Class<? extends T> type) {
    if (yaml instanceof YObject) throw new IllegalArgumentException("Cannot map an instance of " + YObject.class.getSimpleName());
    
    final YMapper mapper = getMapper(type != null ? type : YRuntimeTyped.class);
    final YObject y = new YObject(yaml, this);
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
