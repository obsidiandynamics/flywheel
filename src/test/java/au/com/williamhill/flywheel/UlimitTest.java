package au.com.williamhill.flywheel;

import static org.junit.Assert.*;

import org.junit.*;
import org.slf4j.*;

import com.obsidiandynamics.shell.*;

public final class UlimitTest {
  @Test
  public void test() {
    final int minLimit = 1024;
    final StringBuilder sb = new StringBuilder();
    BourneUtils.run("ulimit -Sn", null, false, sb::append);
    final int limit = Integer.parseInt(sb.toString().trim());
    final Logger logger = LoggerFactory.getLogger(UlimitTest.class);
    logger.debug("File limit is {}", limit);
    assertTrue("Limit is too low " + limit, limit >= minLimit);
  }
}
