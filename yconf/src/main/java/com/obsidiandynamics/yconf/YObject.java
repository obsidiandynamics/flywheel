package com.obsidiandynamics.yconf;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 *  Encapsulates a DOM fragment, as well as the current deserialization context.
 */
public final class YObject {
  private final Object dom;
  
  private final YContext context;

  YObject(Object dom, YContext context) {
    if (dom instanceof YObject) throw new IllegalArgumentException("Cannot wrap another " + YObject.class.getSimpleName());
    this.dom = context.transformDom(dom);
    this.context = context;
  }
  
  public boolean isNull() {
    return dom == null;
  }
  
  public boolean is(Class<?> type) {
    return dom != null && type.isAssignableFrom(dom.getClass());
  }
  
  public <T> T value() {
    return dom != null ? YContext.cast(dom) : null;
  }
  
  private void checkNotNull() {
    if (dom == null) throw new NullPointerException("Wrapping a null DOM");
  }
  
  public List<YObject> asList() {
    checkNotNull();
    return this.<List<?>>value().stream()
        .map(v -> new YObject(v, context))
        .collect(Collectors.toList());
  }
  
  public Map<String, YObject> asMap() {
    checkNotNull();
    class Tuple<K, V> {
      final K k;
      final V v;
      
      Tuple(K k, V v) {
        this.k = k;
        this.v = v;
      }
    }
    return this.<Map<String, ?>>value().entrySet().stream()
        .map(e -> new Tuple<>(e.getKey(), new YObject(e.getValue(), context)))
        .collect(Collectors.toMap(t -> t.k, t -> t.v));
  }
  
  public <T> T map(Class<? extends T> type) {
    return context.map(value(), type);
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
  
  public final class YConditional {
    final YObject y;
    
    YConditional(YObject y) { this.y = y; }
    
    public YObject then(Consumer<YObject> consumer) {
      if (! y.isNull()) {
        consumer.accept(y);
      }
      return YObject.this;
    }
    
    public <T> YObject thenMap(Class<? extends T> type, Consumer<T> consumer) {
      if (! y.isNull()) {
        consumer.accept(y.map(type));
      }
      return YObject.this;
    }
  }
  
  public YConditional when(String att) {
    return new YConditional(getAttribute(att));
  }
  
  @Override
  public String toString() {
    return String.valueOf(dom);
  }
}
