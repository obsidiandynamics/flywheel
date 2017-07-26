package com.obsidiandynamics.yconf;

public final class MappingException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public MappingException(String m) { this(m, null); }
  
  public MappingException(String m, Throwable cause) { super(m, cause); }
}