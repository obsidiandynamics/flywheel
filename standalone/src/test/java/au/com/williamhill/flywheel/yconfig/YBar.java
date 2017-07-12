package au.com.williamhill.flywheel.yconfig;

import java.util.*;
import java.util.stream.*;

@Y(YBar.Mapper.class)
public class YBar {
  static class Mapper implements YMapper {
    @Override
    public Object map(YObject y) {
      final List<YObject> itemsYaml = y.getAttribute("items").list();
      final List<Object> items = itemsYaml.stream().map(itemYaml -> itemYaml.map()).collect(Collectors.toList());
      return new YBar((Integer) y.mapAttribute("num", Object.class), items);
    }
  }
  
  Integer num;
  
  List<Object> items;

  YBar(Integer num, List<Object> items) {
    this.num = num;
    this.items = items;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((items == null) ? 0 : items.hashCode());
    result = prime * result + ((num == null) ? 0 : num.hashCode());
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
    YBar other = (YBar) obj;
    if (items == null) {
      if (other.items != null)
        return false;
    } else if (!items.equals(other.items))
      return false;
    if (num == null) {
      if (other.num != null)
        return false;
    } else if (!num.equals(other.num))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "YBar [num=" + num + ", items=" + items + "]";
  }
}
