package com.obsidiandynamics.yconf;

import static junit.framework.TestCase.*;

import java.io.*;
import java.util.*;

import org.junit.Test;

import junit.framework.*;

public final class YContextTest {
  @Test
  public void test() throws IOException {
    final YFooBar fb = new YContext()
        .withMapper(Object.class, new YRuntimeMapper().withTypeAttribute("_type"))
        .withMapper(YFooBar.class, y -> new YFooBar(y.getAttribute("foo").map(YFoo.class), y.mapAttribute("bar", Object.class)))
        .fromStream(YContextTest.class.getClassLoader().getResourceAsStream("context-test.yaml"), YFooBar.class);

    final YFooBar expected = new YFooBar(new YFoo("A string", 123, false), 
                                         new YBar(42, Arrays.asList(new YFoo("Another string", 456, null),
                                                                    new YFoo(null, 789, null),
                                                                    null,
                                                                    null,
                                                                    new YFoo(null, 789, null))));
    assertEquals(expected, fb);
  }
  
  @Test(expected=YException.class)
  public void testWithoutMapper() throws IOException {
    new YContext()
        .fromStream(YContextTest.class.getClassLoader().getResourceAsStream("context-test.yaml"), YFooBar.class);
  }
  
  public void testWithoutMapperAttribute() throws IOException {
    final String yaml = "a: b";
    final Object obj = new YContext().fromReader(new StringReader(yaml), Object.class);
    final Map<String, String> expected = new LinkedHashMap<>();
    expected.put("a", "b");
    assertEquals(expected, obj);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testWrapObject() {
    new YObject(new YObject("foo", new YContext()), null);
  }
  
  @Test
  public void testObjectToString() {
    assertEquals("foo", new YObject("foo", new YContext()).toString());
  }
  
  @Test(expected=NullPointerException.class)
  public void testNullList() {
    new YObject(null, null).asList();
  }

  @Test(expected=YException.class)
  public void testExplicitTypeNotFound() {
    final String yaml = "type: java.Foo";
    new YContext().fromString(yaml, null);
  }
  
  @Test
  public void testFromString() {
    final String yaml = "a: b";
    new YContext().fromString(yaml, Object.class);
  }
  
  @Test
  public void testFromReader() throws IOException {
    final String yaml = "a: b";
    new YContext().fromReader(new StringReader(yaml), Object.class);
  }
  
  @Y(TestType.Mapper.class)
  private static class TestType {
    static abstract class Mapper implements YMapper {}
  }
  
  @Test(expected=YException.class)
  public void testUninstantiableMapper() {
    final String yaml = "type: " + TestType.class.getName();
    new YContext().fromString(yaml, Object.class);
  }
  
  @Test(expected=NullPointerException.class)
  public void testNullStream() throws IOException {
    new YContext().fromStream(null, null);
  }
  
  @Test(expected=NullPointerException.class)
  public void testNullReader() throws IOException {
    new YContext().fromReader(null, null);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testMapYObject() {
    new YContext().map(new YObject(null, new YContext()), null);
  }
  
  @Test
  public void testGetContext() {
    final Object out = new YContext().withMapper(Void.class, y -> {
      return y.getContext().map(y.getAttribute("f").value(), String.class);
    }).fromString("f: foo", Void.class);
    TestCase.assertEquals("foo", out);
  }
  
  @Test
  public void testIsNotType() {
    final Object out = new YContext().withMapper(Void.class, y -> {
      assertTrue(y.is(Map.class));
      return "done";
    }).fromString("f: foo", Void.class);
    TestCase.assertEquals("done", out);
  }
  
  @Test
  public void testIsType() {
    final Object out = new YContext().withMapper(Void.class, y -> {
      assertFalse(y.is(Integer.class));
      return "done";
    }).fromString("f: foo", Void.class);
    TestCase.assertEquals("done", out);
  }
  
  @Test
  public void testIsTypeWithNull() {
    final Object out = new YContext().withMapper(Void.class, y -> {
      assertFalse(y.getAttribute("a").is(Integer.class));
      return "done";
    }).fromString("f: foo", Void.class);
    TestCase.assertEquals("done", out);
  }
  
  @Test(expected=NullPointerException.class)
  public void testAsListNPE() {
    new YContext().withMapper(Void.class, y -> {
      y.getAttribute("a").asList();
      return null;
    }).fromString("f: foo", Void.class);
  }
}
