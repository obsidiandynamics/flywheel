package com.obsidiandynamics.yconf;

import static org.junit.Assert.*;

import java.net.*;

import org.junit.*;

public final class CoersingMapperTest {
  @Test
  public void testBoolean() {
    test(true, Boolean.class, true);
    test("true", Boolean.class, true);
  }
  
  @Test
  public void testByte() {
    test((byte) 127, Byte.class, (byte) 127);
    test("127", Byte.class, (byte) 127);
  }
  
  @Test
  public void testCharacter() {
    test('a', Character.class, 'a');
    test("b", Character.class, 'b');
  }
  
  @Test(expected=MappingException.class)
  public void testInvalidCharacter() {
    test("abc", Character.class, null);
  }

  @Test
  public void testNull() {
    test(null, Short.class, null);
  }
  
  @Test
  public void testDouble() {
    test(1234.56d, Double.class, 1234.56d);

    final MappingContext context = new MappingContext();
    final Double mapped = context.map("1234.56", Double.class);
    assertEquals(1234.56f, mapped, 0.0001);
  }
  
  @Test
  public void testFloat() {
    test(1234.56f, Float.class, 1234.56f);

    final MappingContext context = new MappingContext();
    final Float mapped = context.map("1234.56", Float.class);
    assertEquals(1234.56f, mapped, 0.0001);
  }
  
  @Test
  public void testInteger() {
    test(1234, Integer.class, 1234);
    test("1234", Integer.class, 1234);
  }
  
  @Test
  public void testLong() {
    test(1234L, Long.class, 1234L);
    test("1234", Long.class, 1234L);
  }
  
  @Test
  public void testShort() {
    test((short) 1234, Short.class, (short) 1234);
    test("1234", Short.class, (short) 1234);
  }
  
  @Test
  public void testString() {
    test("hello", String.class, "hello");
  }
  
  @Test
  public void testUrl() throws MalformedURLException {
    test("http://localhost:8080/text", URL.class, new URL("http://localhost:8080/text"));
  }

  private <T> void test(Object doc, Class<T> type, T expected) {
    final MappingContext context = new MappingContext();
    final Object mapped = context.map(doc, type);
    assertEquals(expected, mapped);
  }
}
