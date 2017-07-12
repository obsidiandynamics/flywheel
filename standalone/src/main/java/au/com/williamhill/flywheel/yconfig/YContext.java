package au.com.williamhill.flywheel.yconfig;

import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

public final class YContext {
  private final Map<Class<?>, YMapper<?, ?>> mappers = new HashMap<>();
  
  private YContext() {}

  private <F, T> YMapper<F, T> getMapper(Class<T> type) {
    final YMapper<?, ?> existing = mappers.get(type);
    if (existing != null) {
      return cast(existing);
    } else {
      final YMapper<F, T> newMapper = instantiateMapper(type);
      mappers.put(type, newMapper);
      return newMapper;
    }
  }
  
  @SuppressWarnings("unchecked")
  private static <T> T cast(Object obj) {
    return (T) obj;
  }
  
  private static <F, T> YMapper<F, T> instantiateMapper(Class<T> type) {
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
  
  public Object map(Object yaml) {
    return map(yaml, YRuntimeTyped.class);
  }
  
  public <T> T map(Object yaml, Class<? extends T> type) {
    final YMapper<Object, ? extends T> mapper = getMapper(type);
    return mapper.map(yaml, this);
  }
  
  public static <T> T fromStream(InputStream in, Class<T> type) throws IOException {
    if (in == null) throw new IllegalArgumentException("Input stream is null");
    final Object root;
    try (InputStream input = in) {
      root = new Yaml().load(input);
    }
    final YContext context = new YContext();
    return context.map(root, type);
  }
}
