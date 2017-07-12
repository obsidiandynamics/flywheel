package au.com.williamhill.flywheel.yconfig;

import java.util.*;
import java.util.stream.*;

public final class YObject {
  private final Object yaml;
  
  private final YContext context;

  YObject(Object yaml, YContext context) {
    if (yaml instanceof YObject) throw new IllegalArgumentException("Cannot wrap another " + YObject.class.getSimpleName());
    this.yaml = yaml;
    this.context = context;
  }
  
  public boolean isNull() {
    return yaml == null;
  }
  
  public <T> T value() {
    return YContext.cast(yaml);
  }
  
  private void checkNotNull() {
    if (isNull()) throw new NullPointerException("Yaml content cannot be null");
  }
  
  public List<YObject> list() {
    checkNotNull();
    return this.<List<?>>value().stream().map(v -> new YObject(v, context)).collect(Collectors.toList());
  }
  
  public <T> T map() {
    return map(null);
  }
  
  public <T> T map(Class<? extends T> type) {
    return context.map(yaml, type);
  }
  
  public YContext getContext() {
    return context;
  }
  
  public YObject getAttribute(String att) {
    checkNotNull();
    return new YObject(this.<Map<?, ?>>value().get(att), context);
  }
  
  public <T> T mapAttribute(String att) {
    return mapAttribute(att, null);
  }
  
  public <T> T mapAttribute(String att, Class<? extends T> type) {
    return context.map(getAttribute(att).value(), type);
  }
  
  @Override
  public String toString() {
    return String.valueOf(yaml);
  }
}
