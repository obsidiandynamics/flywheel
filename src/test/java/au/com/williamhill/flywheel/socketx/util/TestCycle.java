package au.com.williamhill.flywheel.socketx.util;

import java.util.*;

/**
 *  Generates parameters for JUnit's {@link org.junit.runners.Parameterized} class
 *  so that the test case can be repeated a set number of times.
 */
public final class TestCycle {
  private TestCycle() {}
  
  public static List<Object[]> once() {
    return Arrays.asList(new Object[0][0]);
  }
  
  /**
   *  This method assumes that you have an integer variable annotated with 
   *  <code>@Parameter(0)</code>, to which a zero-based run number will be assigned
   *  at the beginning of each iteration.
   *  
   *  @param times The number of times to repeat.
   *  @return The parameter data.
   */
  public static List<Object[]> times(int times) {
    final Object[][] params = new Object[times][1];
    for (int i = 0; i < times; i++) params[i][0] = i;
    return Arrays.asList(params);
  }
}
