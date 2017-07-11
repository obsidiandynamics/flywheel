package au.com.williamhill.flywheel.logging;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;

public final class TestAppender extends WriterAppender {
  public static final ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);
  public static final OutputStreamWriter w = new OutputStreamWriter(baos);

  public TestAppender() {
    setWriter(w);
  }

  public TestAppender(Layout layout) {
    setWriter(w);
    setLayout(layout);
  }
}