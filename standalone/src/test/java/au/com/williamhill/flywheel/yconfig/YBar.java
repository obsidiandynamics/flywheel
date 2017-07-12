package au.com.williamhill.flywheel.yconfig;

import java.util.*;
import java.util.stream.*;

@Y(YBar.Mapper.class)
public class YBar {
  static class Mapper implements YMapper<Map<String, Object>, YBar> {
    @Override
    public YBar map(Map<String, Object> yaml, YContext context) {
      @SuppressWarnings("unchecked")
      final List<Object> itemsYaml = (List<Object>) yaml.get("items");
      return new YBar(itemsYaml.stream().map(itemYaml -> context.map(itemYaml)).collect(Collectors.toList()));
    }
  }
  
  List<Object> items;

  YBar(List<Object> items) {
    this.items = items;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((items == null) ? 0 : items.hashCode());
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
    return true;
  }

  @Override
  public String toString() {
    return "YBar [items=" + items + "]";
  }
}
