package au.com.williamhill.flywheel.util;

import static junit.framework.TestCase.*;

import org.junit.*;
import org.slf4j.*;

import com.obsidiandynamics.assertion.*;

public final class ConditionalMDCTest {
  @Before
  public void setup() {
    MDC.clear();
  }
  
  @After
  public void teardown() {
    MDC.clear();
  }
  
  @Test
  public void testToString() {
    Assertions.assertToStringOverride(ConditionalMDC.on(() -> true));
  }
  
  @Test
  public void testConditionPass() {
    ConditionalMDC.on(() -> true).put("foo", "bar").put("dog", "bark").assign();
    assertEquals("foo,dog", MDC.get("root"));
    assertEquals("bar", MDC.get("foo"));
    assertEquals("bark", MDC.get("dog"));
  }
  
  @Test
  public void testConditionFail() {
    ConditionalMDC.on(() -> false).put("foo", "bar").assign();
    assertNull(MDC.get("root"));
    assertNull(MDC.get("foo"));
    assertNull(MDC.get("dog"));
  }
}
