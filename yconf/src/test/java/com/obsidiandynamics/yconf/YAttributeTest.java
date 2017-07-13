package com.obsidiandynamics.yconf;

import static junit.framework.TestCase.*;

import java.io.*;

import org.junit.*;

public final class YAttributeTest {
  @Y(TestAttributes.Mapper.class)
  public static final class TestAttributes {
    public static final class Mapper implements YMapper {
      @Override public Object map(YObject y) {
        return YSetter.set(y, new TestAttributes());
      }
    }
    
    @YAttribute
    public String str;
    
    @YAttribute(name="number")
    private int num;
    
    @YAttribute(type=Float.class)
    private double dub;
  }

  @Test
  public void test() throws IOException {
//    final TestAttributes t = new YContext()
//        .fromStream(YContextTest.class.getClassLoader().getResourceAsStream("attribute-test.yaml"), 
//                    TestAttributes.class);
//    assertEquals("hello", t.str);
//    assertEquals(1234, t.num);
//    assertEquals(45.67, t.dub, 0.0001);
  }
}
