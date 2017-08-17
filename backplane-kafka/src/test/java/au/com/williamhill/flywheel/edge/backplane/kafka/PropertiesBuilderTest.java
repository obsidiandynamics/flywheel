package au.com.williamhill.flywheel.edge.backplane.kafka;

import static junit.framework.TestCase.*;

import java.util.*;

import org.junit.*;

public final class PropertiesBuilderTest {
  @Test
  public void test() {
    final PropertiesBuilder builder = new PropertiesBuilder()
        .with("foo", "bar");
    
    final Properties props = builder.build();
    assertEquals("bar", props.get("foo"));
    assertEquals(props.toString(), builder.toString());
  }
}
