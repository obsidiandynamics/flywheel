package com.obsidiandynamics.yconf;

import static junit.framework.TestCase.*;

import java.io.*;
import java.util.*;

import org.junit.*;

public final class YAttributeTest {
  public static abstract class TestAttributesSuper {
    @YInject(name="byte", type=byte.class)
    protected byte b;
  }
  
  @Y(TestAttributes.Mapper.class)
  public static final class TestAttributes extends TestAttributesSuper {
    public static final class Mapper implements YMapper {
      @Override public Object map(YObject y, Class<?> type) {
        return y.mapReflectively(new TestAttributes());
      }
    }
    
    @YInject
    public String str;
    
    @YInject(name="number")
    private int num;
    
    @YInject(type=Float.class)
    double dub;
    
    @YInject(type=Object.class)
    List<?> list;
    
    @YInject(type=Object.class)
    Map<?, ?> map;
    
    @YInject
    Class<?> cls;
  }

  @Test
  public void testReflective() throws IOException {
    final TestAttributes t = new YContext()
        .fromStream(YContextTest.class.getClassLoader().getResourceAsStream("attribute-test.yaml"), 
                    TestAttributes.class);
    assertEquals("hello", t.str);
    assertEquals(123, t.num);
    assertEquals(45.67, t.dub, 0.0001);
    assertEquals(-128, t.b);
    assertEquals(Arrays.asList(1, 2, 3), t.list);
    
    final Map<String, String> map = new LinkedHashMap<>();
    map.put("a", "foo");
    map.put("b", "bar");
    assertEquals(map, t.map);
    
    assertEquals(String.class, t.cls);
  }
  
  @Y(TestWrongType.Mapper.class)
  public static final class TestWrongType {
    public static final class Mapper implements YMapper {
      @Override public Object map(YObject y, Class<?> type) {
        return y.mapReflectively(new TestWrongType());
      }
    }

    @YInject(name="byte", type=String.class)
    public boolean b;
  }
  
  @Y(TestClassNotFound.Mapper.class)
  public static final class TestClassNotFound {
    public static final class Mapper implements YMapper {
      @Override public Object map(YObject y, Class<?> type) {
        return y.mapReflectively(new TestClassNotFound());
      }
    }

    @YInject(name="byte")
    public Class<?> cls;
  }

  @Test(expected=YException.class)
  public void testReflectiveWrongType() throws IOException {
    new YContext()
        .fromStream(YContextTest.class.getClassLoader().getResourceAsStream("attribute-test.yaml"), 
                    TestWrongType.class);
  }

  @Test(expected=YException.class)
  public void testReflectiveClassNotFound() throws IOException {
    new YContext()
        .fromStream(YContextTest.class.getClassLoader().getResourceAsStream("attribute-test.yaml"), 
                    TestClassNotFound.class);
  }
}
