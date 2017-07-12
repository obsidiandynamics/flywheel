package au.com.williamhill.flywheel.yconfig;

import java.util.*;
import java.util.stream.*;

/**
 *  Encapsulates a DOM fragment, as well as the current deserialization context.
 */
public final class YObject {
  private final Object dom;
  
  private final YContext context;

  YObject(Object dom, YContext context) {
    if (dom instanceof YObject) throw new IllegalArgumentException("Cannot wrap another " + YObject.class.getSimpleName());
    this.dom = dom;
    this.context = context;
  }
  
  public boolean is(Class<?> type) {
    return dom != null && type.isAssignableFrom(dom.getClass());
  }
  
  public boolean isNull() {
    return dom == null;
  }
  
  public <T> T value() {
    return YContext.cast(dom);
  }
  
  private void checkNotNull() {
    if (isNull()) throw new NullPointerException("Wrapping a null DOM");
  }
  
  public List<YObject> asList() {
    checkNotNull();
    return this.<List<?>>value().stream().map(v -> new YObject(v, context)).collect(Collectors.toList());
  }
  
  public <T> T map(Class<? extends T> type) {
    return context.map(dom, type);
  }
  
  public YContext getContext() {
    return context;
  }
  
  public YObject getAttribute(String att) {
    checkNotNull();
    return new YObject(this.<Map<?, ?>>value().get(att), context);
  }
  
  public <T> T mapAttribute(String att, Class<? extends T> type) {
    return context.map(getAttribute(att).value(), type);
  }
  
  @Override
  public String toString() {
    return String.valueOf(dom);
  }
}
