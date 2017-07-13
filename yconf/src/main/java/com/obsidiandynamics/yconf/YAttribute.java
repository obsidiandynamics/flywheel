package com.obsidiandynamics.yconf;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 *  Instruction to reflectively assign an attribute value from {@link YSetter}.
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface YAttribute {
  String name() default "";
  Class<?> type() default Void.class;
}
