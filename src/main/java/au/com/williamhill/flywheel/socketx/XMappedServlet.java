package au.com.williamhill.flywheel.socketx;

import java.util.*;

import javax.servlet.*;

public final class XMappedServlet {
  private final String servletMapping;
  
  private final String servletName;
  
  private final Class<? extends Servlet> servletClass;
  
  public XMappedServlet(String servletMapping, Class<? extends Servlet> servletClass) {
    this(servletMapping, generateServletName(servletClass), servletClass);
  }
  
  private static String generateServletName(Class<? extends Servlet> servletClass) {
    return servletClass.getSimpleName() + "_" + UUID.randomUUID().toString();
  }

  public XMappedServlet(String servletMapping, String servletName, Class<? extends Servlet> servletClass) {
    this.servletMapping = servletMapping;
    this.servletName = servletName;
    this.servletClass = servletClass;
  }
  
  public String getServletMapping() {
    return servletMapping;
  }
  
  public String getServletName() {
    return servletName;
  }

  public Class<? extends Servlet> getServletClass() {
    return servletClass;
  }

  @Override
  public String toString() {
    return servletMapping + " -> " + servletClass;
  }
}
