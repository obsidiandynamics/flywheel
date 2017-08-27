package au.com.williamhill.flywheel.edge.auth.httpstub;

import org.apache.http.concurrent.*;

public interface FutureCallbackAdapter<T> extends FutureCallback<T> {
  @Override
  public default void completed(T result) {}

  @Override
  public default void failed(Exception ex) {}

  @Override
  public default void cancelled() {}
}
