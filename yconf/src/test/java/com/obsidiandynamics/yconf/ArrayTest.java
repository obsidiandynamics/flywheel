package com.obsidiandynamics.yconf;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

public final class ArrayTest {
  @Y
  static final class Foo {
    int num;
    
    Foo(@YInject(name="num") int num) {
      this.num = num;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + num;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Foo other = (Foo) obj;
      if (num != other.num)
        return false;
      return true;
    }
  }
  
  @Y
  public static class TestArrays {
    @YInject
    Object[] emptyObjArray;
    
    @YInject
    int[] nullIntArray;
    
    @YInject(type=String[].class)
    String[] stringArray;
    
    @YInject(type=String[][].class)
    String[][] string2DArray;
    
    @YInject
    Foo[] fooArray;
  }
  
  @Y
  public static class TestArraysConstructor extends TestArrays {
    TestArraysConstructor(@YInject(name="emptyObjArray") Object[] emptyObjArray, 
                          @YInject(name="nullIntArray") int[] nullIntArray, 
                          @YInject(name="stringArray") String[] stringArray, 
                          @YInject(name="string2DArray") String[][] string2DArray,
                          @YInject(name="fooArray") Foo[] fooArray) {
      this.emptyObjArray = emptyObjArray;
      this.nullIntArray = nullIntArray;
      this.stringArray = stringArray;
      this.string2DArray = string2DArray;
      this.fooArray = fooArray;
      checkAssertions(this);
    }
  }

  @Test
  public void testField() throws IOException {
    final TestArrays t = new MappingContext()
        .fromStream(ArrayTest.class.getClassLoader().getResourceAsStream("array-test.yaml"), TestArrays.class);
    checkAssertions(t);
  }

  @Test
  public void testConstructor() throws IOException {
    final TestArraysConstructor t = new MappingContext()
        .fromStream(ArrayTest.class.getClassLoader().getResourceAsStream("array-test.yaml"), TestArraysConstructor.class);
    checkAssertions(t);
  }

  private static void checkAssertions(TestArrays t) {
    assertArrayEquals(new Object[] {}, t.emptyObjArray);
    assertNull(t.nullIntArray);
    assertArrayEquals(new String[] {"alpha", "bravo", "charlie"}, t.stringArray);
    assertArrayEquals(new String[][] {{"aa", "ab"}, {"ba", "bb"}}, t.string2DArray);
    assertArrayEquals(new Foo[] {new Foo(0), new Foo(1)}, t.fooArray);
  }
}
