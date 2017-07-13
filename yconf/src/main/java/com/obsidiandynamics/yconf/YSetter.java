package com.obsidiandynamics.yconf;

import java.lang.reflect.*;

public final class YSetter {
  private YSetter() {}
  
  public static <T> T set(YObject y, T target) {
    Class<?> cls = target.getClass();
    do {
      for (Field field : cls.getDeclaredFields()) {
        final YAttribute ya = field.getDeclaredAnnotation(YAttribute.class);
        if (ya != null) {
          final String name = ya.name() != null ? ya.name() : field.getName();
          final Class<?> type = ya.type() != null ? ya.type() : field.getType();
          final Object value = y.getAttribute(name).map(type);
          field.setAccessible(true);
          try {
            field.set(target, value);
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new YException("Unable to assign to field " + field.getName() + " of class " + cls, e);
          }
        }
      }
      cls = cls.getSuperclass();
    } while (cls != null);
    return target;
  }
}
