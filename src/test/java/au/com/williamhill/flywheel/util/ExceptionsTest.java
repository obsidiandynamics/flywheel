package au.com.williamhill.flywheel.util;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ExceptionsTest {
  @Test
  public void testConformance() throws Exception {
    Assertions.assertUtilityClassWellDefined(Exceptions.class);
  }
  
  private class TestException extends Exception {
    private static final long serialVersionUID = 1L;
    TestException(Throwable cause) { super(cause); }
    TestException(String m, Throwable cause) { super(m, cause); }
  }

  @Test
  public void testReturnNoThrow() throws TestException {
    final String value = Exceptions.rethrow(() -> "ok", e -> new TestException(e));
    assertEquals("ok", value);
  }

  @Test
  public void testReturnThrow() {
    final Exception cause = new Exception("boom");
    try {
      Exceptions.rethrow(() -> {
        throw cause;
      }, e -> new TestException("thrown", e));
      fail("Expected exception not thrown");
    } catch (TestException e) {
      assertEquals(cause, e.getCause());
      assertEquals("thrown", e.getMessage());
    }
  }
  
  @Test
  public void testVoidNoThrow() throws TestException {
    Exceptions.rethrow(() -> {}, e -> new TestException(e));
  }
  
  @Test
  public void testRootCause() {
    final Exception root = new Exception("root");
    final Exception intermediate = new Exception("intermediate", root);
    assertEquals(root, Exceptions.getRootCause(intermediate));
  }
}
