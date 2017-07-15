package com.obsidiandynamics.yconf;

import java.lang.reflect.*;
import java.util.*;

/**
 *  A {@link TypeMapper} implementation that will attempt to reflectively instantiate a given
 *  class, in addition to setting fields directly post construction.
 */
public final class ReflectiveMapper implements TypeMapper {
  @Override
  public Object map(YObject y, Class<?> type) {
    final Constructor<?> constr;
    try {
      constr = getConstructor(type);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new MappingException("Class " + type.getName() + " does not have a suitable constructor", e);
    }
    
    final Object[] args = new Object[constr.getParameterCount()];
    final Parameter[] params = constr.getParameters();
    for (int i = 0; i < params.length; i++) {
      final YInject inj = params[i].getAnnotation(YInject.class);
      final Class<?> t = inj.type() != Void.class ? inj.type() : params[i].getType();
      if (inj.name().isEmpty()) throw new MappingException("No name specified for attribute of type " + t.getName(), null);
      args[i] = y.getAttribute(inj.name()).map(t);
    }

    final Object target;
    try {
      target = constr.newInstance(args);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new MappingException("Error instantiating " + type.getName(), e);
    }
    
    return y.mapReflectively(target);
  }
  
  private static Constructor<?> getConstructor(Class<?> type) throws NoSuchMethodException, SecurityException {
    for (Constructor<?> c : type.getDeclaredConstructors()) {
      final long count = Arrays.stream(c.getParameters()).filter(p -> p.isAnnotationPresent(YInject.class)).count();
      if (count > 0 && count == c.getParameterCount()) {
        c.setAccessible(true);
        return c;
      }
    }
    
    return type.getConstructor();
  }
}
