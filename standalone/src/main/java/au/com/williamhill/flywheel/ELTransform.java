package au.com.williamhill.flywheel;

import java.lang.reflect.*;
import java.util.function.*;

import javax.el.*;

import de.odysseus.el.*;
import de.odysseus.el.util.*;

public final class ELTransform implements Function<Object, Object> {
  private final ExpressionFactory factory = new ExpressionFactoryImpl();
  private final SimpleContext context = new SimpleContext();
  
  public ELTransform withFunction(String name, Method method) {
    return withFunction("", name,  method);
  }
  
  public ELTransform withFunction(String namespace, String name, Method method) {
    context.setFunction(namespace, name, method);
    return this;
  }
  
  public ELTransform withVariable(String name, Object val) {
    context.setVariable(name, factory.createValueExpression(val, Object.class));
    return this;
  }
  
  @Override
  public Object apply(Object t) {
    if (t instanceof String) {
      final String str = (String) t;
      final ValueExpression expr = factory.createValueExpression(context, str, Object.class);
      return expr.getValue(context);
    } else {
      return t;
    }
  }
}
