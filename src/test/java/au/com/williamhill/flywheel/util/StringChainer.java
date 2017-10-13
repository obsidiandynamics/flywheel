package au.com.williamhill.flywheel.util;

/**
 *  A chainable string builder that allows conditional appending of strings, e.g.:<p/>
 *  
 *  <pre> {@code
 *  new StringChainer("foo").when(someCondition).append(" bar");
 *  } </pre><p/>
 *  
 *  Produces "foo" if <code>someCondition</code> is false and "foo bar" if <code>someCondition</code>
 *  is true.
 */
//TODO remove
public final class StringChainer implements CharSequence {
  private final StringBuilder sb;
  
  public StringChainer() {
    this(new StringBuilder());
  }
  
  public StringChainer(CharSequence cs) {
    this(new StringBuilder(cs));
  }
  
  public StringChainer(StringBuilder sb) {
    this.sb = sb;
  }
  
  public StringChainer append(Object obj) {
    sb.append(obj);
    return this;
  }
  
  public ConditionalStringChainer when(boolean condition) {
    return new ConditionalStringChainer(condition);
  }
  
  public final class ConditionalStringChainer {
    private final boolean conditionMet;

    ConditionalStringChainer(boolean conditionMet) {
      this.conditionMet = conditionMet;
    }
    
    public StringChainer append(Object obj) {
      if (conditionMet) {
        StringChainer.this.append(obj);
      }
      return StringChainer.this;
    }
  }

  @Override
  public int length() {
    return sb.length();
  }

  @Override
  public char charAt(int index) {
    return sb.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return sb.subSequence(start, end);
  }
  
  @Override
  public String toString() {
    return sb.toString();
  }
}
