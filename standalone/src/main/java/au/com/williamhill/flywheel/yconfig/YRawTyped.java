package au.com.williamhill.flywheel.yconfig;

@Y(YRawTyped.Mapper.class)
public interface YRawTyped {
  static final class Mapper implements YMapper {
    @Override
    public Object map(YObject y) {
      return y.value();
    }
  }
}
