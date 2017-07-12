package au.com.williamhill.flywheel.yconfig;

import java.util.*;

@Y(YFoo.Mapper.class)
public class YFoo {
  static class Mapper implements YMapper<Map<String, Object>, YFoo> {
    @Override
    public YFoo map(Map<String, Object> yaml, YContext context) {
      System.out.println("yaml=" + yaml);
      return new YFoo((String) yaml.get("a"),
                      (Integer) yaml.get("b"));
    }
  }
  
  String a;
  int b;
  
  YFoo(String a, int b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((a == null) ? 0 : a.hashCode());
    result = prime * result + b;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    YFoo other = (YFoo) obj;
    if (a == null) {
      if (other.a != null)
        return false;
    } else if (!a.equals(other.a))
      return false;
    if (b != other.b)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "YFoo [a=" + a + ", b=" + b + "]";
  }
}
