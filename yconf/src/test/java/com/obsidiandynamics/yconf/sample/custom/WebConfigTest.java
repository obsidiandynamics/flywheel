package com.obsidiandynamics.yconf.sample.custom;

import static junit.framework.TestCase.*;

import java.io.*;
import java.net.*;

import org.junit.*;

import com.obsidiandynamics.yconf.*;

public final class WebConfigTest {
  private static WebConfig load(String filename) throws IOException {
    return new MappingContext()
        .fromStream(WebConfigTest.class.getClassLoader()
                    .getResourceAsStream(filename), WebConfig.class);
  }
  
  @Test
  public void testGood() throws IOException, URISyntaxException {
    final WebConfig conf = load("sample-custom-good.yaml");
    assertNotNull(conf);
    assertEquals(new URI("http://localhost:8080/health"), conf.servers.get("Health check"));
    assertEquals(new URI("ws://broker.acme.com/broker"), conf.servers.get("Message broker"));
    assertEquals(new URI("https://sd.acme.com"), conf.servers.get("Service discovery"));
  }
  
  @Test(expected=MappingException.class)
  public void testDuplicate() throws IOException, URISyntaxException {
    load("sample-custom-duplicate.yaml");
  }
}
