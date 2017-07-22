package au.com.williamhill.flywheel.health;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;
import org.mockito.*;

public final class HealthServletTest {
  @Test
  public void test() throws ServletException, IOException {
    final HealthServlet health = new HealthServlet();
    final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
    final PrintWriter writer = Mockito.mock(PrintWriter.class);
    Mockito.when(res.getWriter()).thenReturn(writer);
    health.doGet(req, res);
    Mockito.verify(writer).write(Mockito.anyString());
  }
}
