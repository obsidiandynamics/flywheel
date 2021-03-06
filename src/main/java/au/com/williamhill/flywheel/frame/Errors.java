package au.com.williamhill.flywheel.frame;

import java.util.*;

public final class Errors {
  private final Error[] errors;
  
  public Errors(Collection<? extends Error> errors) {
    this(errors.toArray(new Error[errors.size()]));
  }

  private Errors(Error ... errors) {
    this.errors = errors;
  }
  
  public Error[] getErrors() {
    return errors;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(errors);
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
    Errors other = (Errors) obj;
    if (!Arrays.equals(errors, other.errors))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Errors [errors=" + Arrays.toString(errors) + "]";
  }
}
