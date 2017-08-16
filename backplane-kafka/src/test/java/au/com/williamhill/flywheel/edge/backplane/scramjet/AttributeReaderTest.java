package au.com.williamhill.flywheel.edge.backplane.scramjet;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

public final class AttributeReaderTest {
  private AttributeReader reader;
  
  @Before
  public void before() {
    final Map<String, Object> atts = new HashMap<>();
    atts.put("foo", "bar");
    atts.put("$type", "type");
    reader = new AttributeReader(atts);
  }
  
  @Test
  public void testReadNoDefault() {
    assertEquals("bar", reader.read("foo"));
    assertNull(reader.read("baz"));
  }
  
  @Test
  public void testReadDefault() {
    assertEquals("default", reader.read("baz", () -> "default"));
  }
  
  @Test
  public void testGetTypeName() {
    assertEquals("type", reader.getTypeName());
  }
  
  @Test
  public void testGetAttributes() {
    assertEquals("bar", reader.getAttributes().get("foo"));
  }
}
