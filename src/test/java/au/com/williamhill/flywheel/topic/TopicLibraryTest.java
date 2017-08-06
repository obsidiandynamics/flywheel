package au.com.williamhill.flywheel.topic;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

public final class TopicLibraryTest {
  @Test
  public void testConformance() throws Exception {
    TestSupport.assertUtilityClassWellDefined(TopicLibrary.class);
  }
  
  @Test
  public void testFile() throws FileNotFoundException, IOException, URISyntaxException {
    assertSpec(TopicLibrary.load("file://src/test/resources/specs/tiny-all.yaml"));
  }
  
  @Test
  public void testClasspath() throws FileNotFoundException, IOException, URISyntaxException {
    assertSpec(TopicLibrary.load("cp://specs/tiny-all.yaml"));
    assertSpec(TopicLibrary.load("classpath://specs/tiny-all.yaml"));
  }
  
  private static void assertSpec(TopicSpec spec) {
    assertEquals(1, spec.getExactInterests().size());
    assertEquals(1, spec.getSingleLevelWildcardInterests().size());
    assertEquals(1, spec.getMultiLevelWildcardInterests().size());
  }
}
