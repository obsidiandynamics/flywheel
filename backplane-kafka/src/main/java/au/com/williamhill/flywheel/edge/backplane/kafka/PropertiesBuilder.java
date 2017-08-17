package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

import com.obsidiandynamics.yconf.*;

@Y(PropertiesBuilder.Mapper.class)
public final class PropertiesBuilder {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      final PropertiesBuilder builder = new PropertiesBuilder();
      for (Map.Entry<String, YObject> entry : y.asMap().entrySet()) {
        builder.with(entry.getKey(), entry.getValue().map(Object.class));
      }
      return builder;
    }
  }
  
  private final Properties properties = new Properties();
  
  public PropertiesBuilder with(String key, Object value) {
    if (value != null) properties.put(key, value.toString());
    return this;
  }
  
  public Properties build() {
    final Properties copy = new Properties();
    copy.putAll(properties);
    return copy;
  }
  
  @Override
  public String toString() {
    return properties.toString();
  }
}