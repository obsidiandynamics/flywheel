package au.com.williamhill.flywheel.edge.auth;

public class CachedAuthenticatorConfig {
  long runIntervalMillis;
  
  long queryBeforeExpiryMillis;
  
  long minQueryIntervalMillis;
  
  int maxPendingQueries;

  public CachedAuthenticatorConfig withRunIntervalMillis(long runIntervalMillis) {
    this.runIntervalMillis = runIntervalMillis;
    return this;
  }

  public CachedAuthenticatorConfig withQueryBeforeExpiryMillis(long queryBeforeExpiryMillis) {
    this.queryBeforeExpiryMillis = queryBeforeExpiryMillis;
    return this;
  }

  public CachedAuthenticatorConfig withMinQueryIntervalMillis(long minQueryIntervalMillis) {
    this.minQueryIntervalMillis = minQueryIntervalMillis;
    return this;
  }
  
  public CachedAuthenticatorConfig withMaxPendingQueries(int maxPendingQueries) {
    this.maxPendingQueries = maxPendingQueries;
    return this;
  }

  @Override
  public String toString() {
    return "CachedAuthenticatorConfig [runIntervalMillis: " + runIntervalMillis + ", queryBeforeExpiryMillis: "
           + queryBeforeExpiryMillis + ", minQueryIntervalMillis: " + minQueryIntervalMillis 
           + ", maxPendingQueries: " + maxPendingQueries + "]";
  }
}
