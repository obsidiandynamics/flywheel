package au.com.williamhill.flywheel.socketx;

import java.util.*;

import javax.servlet.*;

public final class XMappedServlet {
  private final String path;
  
  private final String name;
  
  private final Class<? extends Servlet> servletClass;
  
  public XMappedServlet(String path, Class<? extends Servlet> servletClass) {
    this(path, generateServletName(servletClass), servletClass);
  }
  
  private static String generateServletName(Class<? extends Servlet> servletClass) {
    return servletClass.getSimpleName() + "_" + UUID.randomUUID().toString();
  }

  public XMappedServlet(String path, String name, Class<? extends Servlet> servletClass) {
    this.path = path;
    this.name = name;
    this.servletClass = servletClass;
  }
  
  public String getPath() {
    return path;
  }
  
  public String getName() {
    return name;
  }

  public Class<? extends Servlet> getServletClass() {
    return servletClass;
  }

  @Override
  public String toString() {
    return path + " -> " + servletClass;
  }
}
