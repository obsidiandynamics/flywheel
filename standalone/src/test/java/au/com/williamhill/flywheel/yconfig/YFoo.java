package au.com.williamhill.flywheel.yconfig;

@Y(YFoo.Mapper.class)
public class YFoo {
  static class Mapper implements YMapper {
    @Override
    public Object map(YObject y) {
      return new YFoo(y.mapAttribute("a", String.class),
                      y.mapAttribute("b", Integer.class),
                      y.mapAttribute("c", Boolean.class));
    }
  }
  
  String a;
  int b;
  Boolean c;
  
  YFoo(String a, int b, Boolean c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((a == null) ? 0 : a.hashCode());
    result = prime * result + b;
    result = prime * result + ((c == null) ? 0 : c.hashCode());
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
    if (c == null) {
      if (other.c != null)
        return false;
    } else if (!c.equals(other.c))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "YFoo [a=" + a + ", b=" + b + ", c=" + c + "]";
  }
}
