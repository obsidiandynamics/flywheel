package com.obsidiandynamics.yconf.sample.basic.constructor;

import com.obsidiandynamics.yconf.*;

@Y
public class Top {
  @Y
  public static class Inner {
    @YInject
    final boolean aBool;
    
    @YInject
    final String[] anArray;

    Inner(@YInject(name="aBool") boolean aBool, 
          @YInject(name="anArray") String[] anArray) {
      this.aBool = aBool;
      this.anArray = anArray;
    }
  }
  
  @YInject
  final String aString;
  
  @YInject
  final double aNumber;
  
  @YInject
  final Inner inner;

  Top(@YInject(name="aString") String aString,
      @YInject(name="aNumber") double aNumber, 
      @YInject(name="inner") Inner inner) {
    this.aString = aString;
    this.aNumber = aNumber;
    this.inner = inner;
  }
}
