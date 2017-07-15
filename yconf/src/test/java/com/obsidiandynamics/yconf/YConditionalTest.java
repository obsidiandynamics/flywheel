package com.obsidiandynamics.yconf;

import java.util.concurrent.atomic.*;

import org.junit.Test;

import junit.framework.*;

public final class YConditionalTest {
  @Test
  public void testConditionalValueSet() {
    final AtomicReference<?> out = new MappingContext().withMapper(AtomicReference.class, (y, type) -> {
      final AtomicReference<?> ref = new AtomicReference<>("original");
      y.when("f").then(v -> ref.set(v.value()));
      return ref;
    }).fromString("f: foo", AtomicReference.class);
    TestCase.assertEquals("foo", out.get());
  }

  @Test
  public void testConditionalValueNotSet() {
    final AtomicReference<?> out = new MappingContext().withMapper(AtomicReference.class, (y, type) -> {
      final AtomicReference<?> ref = new AtomicReference<>("original");
      y.when("x").then(v -> ref.set(v.value()));
      return ref;
    }).fromString("f: foo", AtomicReference.class);
    TestCase.assertEquals("original", out.get());
  }

  @Test
  public void testConditionalMapSet() {
    final AtomicReference<?> out = new MappingContext().withMapper(AtomicReference.class, (y, type) -> {
      final AtomicReference<Object> ref = new AtomicReference<>("original");
      y.when("f").thenMap(Object.class, ref::set);
      return ref;
    }).fromString("f: foo", AtomicReference.class);
    TestCase.assertEquals("foo", out.get());
  }

  @Test
  public void testConditionalMapNotSet() {
    final AtomicReference<?> out = new MappingContext().withMapper(AtomicReference.class, (y, type) -> {
      final AtomicReference<Object> ref = new AtomicReference<>("original");
      y.when("x").thenMap(Object.class, ref::set);
      return ref;
    }).fromString("f: foo", AtomicReference.class);
    TestCase.assertEquals("original", out.get());
  }
}
