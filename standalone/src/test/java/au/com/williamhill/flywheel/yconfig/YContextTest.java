package au.com.williamhill.flywheel.yconfig;

import static junit.framework.TestCase.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

public final class YContextTest implements TestSupport {
  @Test
  public void test() throws IOException {
    final YFooBar fb = YContext.fromStream(YContextTest.class.getClassLoader().getResourceAsStream("test.yaml"), YFooBar.class);
    log("fb=%s\n", fb);
    
    final YFooBar expected = new YFooBar(new YFoo("A string", 123), 
                                         new YBar(Arrays.asList(new YFoo("Another string", 456))));
    assertEquals(expected, fb);
  }
}
