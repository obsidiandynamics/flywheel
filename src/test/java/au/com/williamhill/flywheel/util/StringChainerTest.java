package au.com.williamhill.flywheel.util;

import static org.junit.Assert.*;

import org.junit.*;

public final class StringChainerTest {
  @Test
  public void testConditionFail() {
    final CharSequence cs = new StringChainer()
        .append("foo")
        .when(false).append(" bar");
    assertEquals("foo", cs.toString());
  }

  @Test
  public void testConditionPass() {
    final CharSequence cs = new StringChainer("foo")
        .when(true).append(" bar");
    assertEquals("foo bar", cs.toString());
  }
}
