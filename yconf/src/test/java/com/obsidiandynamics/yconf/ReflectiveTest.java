package com.obsidiandynamics.yconf;

import static junit.framework.TestCase.*;

import java.io.*;
import java.util.*;

import org.junit.*;

public final class ReflectiveTest {
  private static final String FILE = "reflective-test.yaml";
  
  public static abstract class Super {
    @YInject(name="byte", type=byte.class)
    protected byte b;
  }
  
  @Y(Mid.Mapper.class)
  public static class Mid extends Super {
    public static final class Mapper implements TypeMapper {
      @Override public Object map(YObject y, Class<?> type) {
        return y.mapReflectively(new Mid());
      }
    }
    
    @YInject
    public String str;
    
    @YInject(type=Float.class)
    double dub;
    
    @YInject(type=Object.class)
    List<?> list;
    
    @YInject(type=Object.class)
    Map<?, ?> map;
    
    @YInject
    Class<?> cls;
  }
  
  @Y(CustomConstruction.Mapper.class)
  public static final class CustomConstruction extends Mid {
    public static final class Mapper implements TypeMapper {
      @Override public Object map(YObject y, Class<?> type) {
        return y.mapReflectively(new CustomConstruction());
      }
    }
    
    @YInject(name="number")
    private int num;
  }

  @Test
  public void testInjectAttributesCustomConstruction() throws IOException {
    final CustomConstruction t = new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    CustomConstruction.class);
    checkAssertions(t);
    assertEquals(123, t.num);
  }
  
  private static void checkAssertions(Mid m) {
    assertEquals("hello", m.str);
    assertEquals(45.67, m.dub, 0.0001);
    assertEquals(-128, m.b);
    assertEquals(Arrays.asList(1, 2, 3), m.list);
    
    final Map<String, String> map = new LinkedHashMap<>();
    map.put("a", "foo");
    map.put("b", "bar");
    assertEquals(map, m.map);
    
    assertEquals(String.class, m.cls);
  }
  
  @Y(TestWrongType.Mapper.class)
  public static final class TestWrongType {
    public static final class Mapper implements TypeMapper {
      @Override public Object map(YObject y, Class<?> type) {
        return y.mapReflectively(new TestWrongType());
      }
    }

    @YInject(name="byte", type=String.class)
    public boolean b;
  }
  
  @Y(TestClassNotFound.Mapper.class)
  public static final class TestClassNotFound {
    public static final class Mapper implements TypeMapper {
      @Override public Object map(YObject y, Class<?> type) {
        return y.mapReflectively(new TestClassNotFound());
      }
    }

    @YInject(name="byte")
    public Class<?> cls;
  }

  @Test(expected=MappingException.class)
  public void testReflectiveWrongType() throws IOException {
    new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    TestWrongType.class);
  }

  @Test(expected=MappingException.class)
  public void testReflectiveClassNotFound() throws IOException {
    new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    TestClassNotFound.class);
  }
  
  @Y(ReflectiveMapper.class)
  public static final class DefaultConstructor extends Mid {
    @YInject(name="number")
    private int num;
  }

  @Test
  public void testDefaultConstructor() throws IOException {
    final DefaultConstructor t = new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    DefaultConstructor.class);
    checkAssertions(t);
    assertEquals(123, t.num);
  }
  
  @Y(ReflectiveMapper.class)
  public static final class AnnotatedConstructorMinimal extends Mid {
    private int num;
    
    AnnotatedConstructorMinimal(@YInject(name="number") int num) {
      this.num = num;
    }
  }

  @Test
  public void testAnnotatedConstructorMinimal() throws IOException {
    final AnnotatedConstructorMinimal t = new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    AnnotatedConstructorMinimal.class);
    checkAssertions(t);
    assertEquals(123, t.num);
  }
  
  @Y(ReflectiveMapper.class)
  public static final class AnnotatedConstructorComplete extends Mid {
    private int num;
    
    AnnotatedConstructorComplete(@YInject(name="byte", type=byte.class) byte b,
                                 @YInject(name="str") String str,
                                 @YInject(name="dub", type=Float.class) double dub,
                                 @YInject(name="list", type=Object.class) List<?> list,
                                 @YInject(name="map", type=Object.class) Map<?, ?> map,
                                 @YInject(name="cls") Class<?> cls,
                                 @YInject(name="number") int num) {
      this.b = b;
      this.str = str;
      this.dub = dub;
      this.list = list;
      this.map = map;
      this.cls = cls;
      this.num = num;
    }
  }

  @Test
  public void testAnnotatedConstructorComplete() throws IOException {
    final AnnotatedConstructorComplete t = new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    AnnotatedConstructorComplete.class);
    checkAssertions(t);
    assertEquals(123, t.num);
  }

  @Y(ReflectiveMapper.class)
  public static final class AnnotatedConstructorNoName extends Mid {
    AnnotatedConstructorNoName(@YInject int num) {}
  }

  @Test(expected=MappingException.class)
  public void testAnnotatedConstructorNoName() throws IOException {
    new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    AnnotatedConstructorNoName.class);
  }

  @Y(ReflectiveMapper.class)
  public static final class AnnotatedConstructorNoDefaultConstructor extends Mid {
    AnnotatedConstructorNoDefaultConstructor(int num) {}
  }

  @Test(expected=MappingException.class)
  public void testAnnotatedConstructorNoDefaultConstructor() throws IOException {
    new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    AnnotatedConstructorNoDefaultConstructor.class);
  }

  @Y(ReflectiveMapper.class)
  public static final class AnnotatedConstructorPartial extends Mid {
    AnnotatedConstructorPartial(@YInject(name="number") int num, String str) {}
  }

  @Test(expected=MappingException.class)
  public void testAnnotatedConstructorPartial() throws IOException {
    new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    AnnotatedConstructorPartial.class);
  }

  @Y(ReflectiveMapper.class)
  public static final class AnnotatedConstructorPrivate extends Mid {
    private AnnotatedConstructorPrivate() {}
  }

  @Test(expected=MappingException.class)
  public void testAnnotatedConstructorPrivate() throws IOException {
    new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    AnnotatedConstructorPrivate.class);
  }

  @Y(ReflectiveMapper.class)
  public static final class AnnotatedConstructorIllegalArg extends Mid {
    AnnotatedConstructorIllegalArg(@YInject(name="number", type=int.class) char num) {}
  }

  @Test(expected=MappingException.class)
  public void testAnnotatedConstructorIllegalArg() throws IOException {
    new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    AnnotatedConstructorIllegalArg.class);
  }

  @Y
  public static final class AttributeDefaultValue extends Mid {
    @YInject(name="nonExistent")
    String defStr = "defaultValue";
  }

  @Test
  public void testAttributeDefaultValue() throws IOException {
    final AttributeDefaultValue t = new MappingContext()
        .fromStream(MappingContextTest.class.getClassLoader().getResourceAsStream(FILE), 
                    AttributeDefaultValue.class);
    checkAssertions(t);
    assertEquals("defaultValue", t.defStr);
  }
}
