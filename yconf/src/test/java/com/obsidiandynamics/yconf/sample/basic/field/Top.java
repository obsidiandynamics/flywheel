package com.obsidiandynamics.yconf.sample.basic.field;

import com.obsidiandynamics.yconf.*;

@Y
public class Top {
  @Y
  public static class Inner {
    @YInject
    boolean aBool;
    
    @YInject
    String[] anArray;
  }
  
  @YInject
  String aString = "some default";
  
  @YInject
  double aNumber;
  
  @YInject
  Inner inner;
}
