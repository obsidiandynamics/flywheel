package au.com.williamhill.flywheel.util;

import static org.mockito.Mockito.*;

import java.util.function.*;

import org.mockito.*;

public final class Ordered {
  private Ordered() {}

  public static void of(Object mock, Consumer<InOrder> test) {
    final InOrder inOrder = inOrder(mock);
    test.accept(inOrder);
    inOrder.verifyNoMoreInteractions();
  }
}
