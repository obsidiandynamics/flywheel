package com.obsidiandynamics.yconf;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.yconf.Mandatory.*;

public final class MandatoryTest {
  @Test
  public void testConformance() throws Exception {
    TestSupport.assertUtilityClassWellDefined(Mandatory.class);
  }
  
  @Test
  public void testNonNullValue() {
    final Object val = "test";
    assertEquals(val, Mandatory.of(val, "error"));
  }

  @Test(expected=MissingValueException.class)
  public void testNullValue() {
    Mandatory.of(null, "error");
  }
  
  @Test
  public void testMapperNonNullValue() {
    final Object obj = new MappingContext().fromString("{value: test, error: bad}", Mandatory.class);
    assertEquals("test", obj);
  }
  
  @Test(expected=MissingValueException.class)
  public void testMapperNullValue() {
    new MappingContext().fromString("{error: bad}", Mandatory.class);
  }
}
