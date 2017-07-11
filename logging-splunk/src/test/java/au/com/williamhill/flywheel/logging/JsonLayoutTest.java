package au.com.williamhill.flywheel.logging;

import static org.junit.Assert.*;

import org.apache.log4j.*;
import org.junit.*;

/**
 *  Adapted from https://github.com/michaeltandy/log4j-json.
 */
public final class JsonLayoutTest {
  private static Logger LOG;
  
  @BeforeClass
  public static void beforeClass() {
    System.setProperty("log4j.configuration", "log4j-test.properties");
    LOG = Logger.getLogger(JsonLayoutTest.class);
  }
  
  @AfterClass
  public static void afterClass() {
    System.clearProperty("log4j.configuration");
    LOG = null;
  }

  @Test
  public void testDemonstration() {
    TestAppender.baos.reset();

    LOG.info("Example of some logging");
    LOG.warn("Some text\nwith a newline", new Exception("Outer Exception", new Exception("Nested Exception")));
    LOG.fatal("Text may be complicated & have many symbols\nÂ¬!Â£$%^&*()_+{}:@~<>?,./;'#[]-=`\\| \t\n");

    final String whatWasLogged = TestAppender.baos.toString();
    final String[] lines = whatWasLogged.split("\n");

    assertEquals(3,lines.length);
    assertTrue(lines[0].contains("INFO"));
    assertTrue(lines[0].contains("Example of some logging"));

    assertTrue(lines[1].contains("newline"));
    assertTrue(lines[1].contains("Outer Exception"));
    assertTrue(lines[1].contains("Nested Exception"));

    assertTrue(lines[2].contains("have many symbols"));
  }

  @Test
  public void testObjectHandling() {
    TestAppender.baos.reset();

    LOG.info(new Object() {
      @Override
      public String toString() {
        throw new RuntimeException("Hypothetical failure");
      }
    });
    LOG.warn(null);

    final String whatWasLogged = TestAppender.baos.toString();
    final String[] lines = whatWasLogged.split("\n");
    assertEquals(2,lines.length);

    assertTrue(lines[0].contains("Hypothetical"));
    assertTrue(lines[1].contains("WARN"));
  }

  @Test
  public void testLogMethod() {
    // Test for https://github.com/michaeltandy/log4j-json/issues/1
    TestAppender.baos.reset();

    LOG.log("asdf", Level.INFO, "this is the log message", null);

    final String whatWasLogged = TestAppender.baos.toString();
    final String[] lines = whatWasLogged.split("\n");
    assertEquals(1,lines.length);
    assertTrue(lines[0].contains("this is the log message"));
  }

  @Test
  public void testMinimumLevelForSlowLogging() {
    TestAppender.baos.reset();

    LOG.info("Info level logging");
    LOG.debug("Debug level logging");

    final String whatWasLogged = TestAppender.baos.toString();
    final String[] lines = whatWasLogged.split("\n");
    assertEquals(2,lines.length);

    assertTrue(lines[0].contains("INFO"));
    assertTrue(lines[0].contains("class"));
    assertTrue(lines[0].contains("line"));
    assertTrue(lines[0].contains("method"));

    assertTrue(lines[1].contains("DEBUG"));
    assertFalse(lines[1].contains("class"));
    assertFalse(lines[1].contains("line"));
    assertFalse(lines[1].contains("method"));
  }

  @Test
  public void testSelectiveMdcLogging() {
    TestAppender.baos.reset();

    MDC.put("asdf", "value_for_key_asdf");
    MDC.put("qwer", "value_for_key_qwer");
    MDC.put("thread", "attempt to overwrite thread in output");

    LOG.info("Example of some logging");

    MDC.clear();

    final String whatWasLogged = TestAppender.baos.toString();
    final String[] lines = whatWasLogged.split("\n");

    assertEquals(1,lines.length);
    assertTrue(lines[0].contains("value_for_key_asdf"));
    assertFalse(lines[0].contains("value_for_key_qwer"));

    assertTrue(lines[0].contains("thread"));
    assertFalse(lines[0].contains("attempt to overwrite thread in output"));
  }
}
