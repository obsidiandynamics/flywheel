package com.obsidiandynamics.yconf;

public final class YException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public YException(String m, Throwable cause) { super(m, cause); }
}