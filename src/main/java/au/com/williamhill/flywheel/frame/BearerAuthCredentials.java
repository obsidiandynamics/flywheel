package au.com.williamhill.flywheel.frame;

public final class BearerAuthCredentials implements AuthCredentials {
  public static String JSON_TYPE_NAME = "Bearer";
  
  private final String token;
  
  public BearerAuthCredentials(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((token == null) ? 0 : token.hashCode());
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
    BearerAuthCredentials other = (BearerAuthCredentials) obj;
    if (token == null) {
      if (other.token != null)
        return false;
    } else if (!token.equals(other.token))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Bearer [token=<masked>]";
  }
}
