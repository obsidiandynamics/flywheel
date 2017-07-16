package com.obsidiandynamics.yconf;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 *  Encapsulates a DOM fragment, as well as the current {@link MappingContext}.
 */
public final class YObject {
  private final Object dom;
  
  private final MappingContext context;

  YObject(Object dom, MappingContext context) {
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
    return dom != null ? MappingContext.cast(dom) : null;
  }
  
  private void checkNotNull() {
    if (dom == null) throw new NullPointerException("Wrapping a null DOM");
  }
  
  public List<YObject> asList() {
    checkNotNull();
    final List<?> items = (List<?>) dom;
    final List<YObject> list = new ArrayList<>(items.size());
    for (Object i : items) {
      list.add(new YObject(i, context));
    }
    return list;
  }
  
  public Map<String, YObject> asMap() {
    checkNotNull();
    final Map<?, ?> items = (Map<?, ?>) dom;
    final Map<String, YObject> map = new LinkedHashMap<>(items.size());
    for (Map.Entry<?, ?> i : items.entrySet()) {
      map.put((String) i.getKey(), new YObject(i.getValue(), context));
    }
    return map;
  }
  
  public <T> T map(Class<? extends T> type) { 
    return context.map(value(), type);
  }
  
  public MappingContext getContext() {
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
  
  /**
   *  Reflectively maps this DOM to the fields of the given target object, assigning
   *  all declared fields across the entire class hierarchy that have been annotated
   *  with {@link YInject}.<p>
   *  
   *  This method will attempt to map only non-null attributes in the DOM. If an attribute
   *  is null, the field of the target object will not be assigned (preserving its default
   *  value). 
   *  
   *  @param <T> The target type.
   *  @param target The target object to populate.
   *  @return The pass-through instance of the {@code target} parameter.
   */
  public <T> T mapReflectively(T target) {
    Class<?> cls = target.getClass();
    do {
      for (Field field : cls.getDeclaredFields()) {
        final YInject inj = field.getDeclaredAnnotation(YInject.class);
        if (inj != null) {
          final String name = ! inj.name().isEmpty() ? inj.name() : field.getName();
          final Class<?> type = inj.type() != Void.class ? inj.type() : field.getType();
          final Object value = getAttribute(name).map(type);
          
          if (value != null) {
            field.setAccessible(true);
            try {
              field.set(target, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
              throw new MappingException("Unable to assign to field " + field.getName() + " of class " + cls, e);
            }
          }
        }
      }
      cls = cls.getSuperclass();
    } while (cls != null);
    return target;
  }
  
  @Override
  public String toString() {
    return String.valueOf(dom);
  }
}
