package au.com.williamhill.flywheel.util;

import java.util.function.*;

/**
 *  Functional equivalent of a try-catch block, as well as complimentary utilities.
 */
public final class Exceptions {
  private Exceptions() {}
  
  /**
   *  A {@link Runnable} that may throw an exception.
   */
  @FunctionalInterface
  public interface ThrowingRunnable extends ThrowingSupplier<Void> {
    void run() throws Exception;
    
    @Override default Void create() throws Exception {
      run();
      return null;
    }
  }
  
  /**
   *  A {@link Supplier} that may throw an exception.
   *  
   *  @param <T> The return type.
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T> {
    T create() throws Exception;
  }
  
  /**
   *  Invokes a given block. If an exception is thrown, it is
   *  first passed through the given {@code exceptionManager} mapping {@link Function} before being rethrown.
   *  
   *  @param <X> The exterior exception type.
   *  @param runnable The runnable block.
   *  @param exceptionMapper The exception mapper (from the interior exception to the exterior exception type).
   *  @throws X The exterior exception if the {@code runnable} block faulted.
   */  
  public static <X extends Exception> void rethrow(ThrowingRunnable runnable, Function<Exception, X> exceptionMapper) throws X {
    rethrow((ThrowingSupplier<Void>) runnable, exceptionMapper);
  }
  
  /**
   *  Invokes a given supplier block returning the outcome if successful. Otherwise, if an exception is thrown, it is
   *  first passed through the given {@code exceptionManager} mapping {@link Function} before being rethrown.
   *  
   *  @param <T> The return type.
   *  @param <X> The exterior exception type.
   *  @param supplier The supplier block.
   *  @param exceptionMapper The exception mapper (from the interior exception to the exterior exception type).
   *  @return The value returned by the {@code supplier} block.
   *  @throws X The exterior exception if the {@code supplier} block faulted.
   */
  public static <T, X extends Exception> T rethrow(ThrowingSupplier<T> supplier, Function<Exception, X> exceptionMapper) throws X {
    try {
      return supplier.create();
    } catch (Exception e) {
      throw exceptionMapper.apply(e);
    }
  }
  
  /**
   *  Obtains the root cause of a given exception.
   *  
   *  @param throwable The exception.
   *  @return The root cause.
   */
  public static Throwable getRootCause(Throwable throwable) {
    Throwable cause = throwable;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }
}
