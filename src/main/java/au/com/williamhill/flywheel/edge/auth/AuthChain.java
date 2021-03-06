package au.com.williamhill.flywheel.edge.auth;

import static au.com.williamhill.flywheel.topic.Topic.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.NestedAuthenticator.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.topic.*;

public abstract class AuthChain<A extends AuthChain<A>> implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(AuthChain.class);
  
  abstract static class AuthChainMapper<A extends AuthChain<A>> implements TypeMapper {
    abstract AuthChain<A> getBaseChain();

    @Override
    public Object map(YObject y, Class<?> type) {
      final AuthChain<A> chain = getBaseChain();
      for (Map.Entry<String, YObject> entry : y.asMap().entrySet()) {
        chain.set(entry.getKey(), entry.getValue().map(Authenticator.class));
      }
      return chain;
    }
  }
  
  public static final class CombinedMatches {
    final static CombinedMatches EMPTY = new CombinedMatches(Collections.emptyList(), 0);
    
    public final List<MatchedAuthenticators> matches;
    public final int numAuthenticators;
    
    CombinedMatches(List<MatchedAuthenticators> matches, int numAuthenticators) {
      this.matches = matches;
      this.numAuthenticators = numAuthenticators;
    }
    
    public void invokeAll(EdgeNexus nexus, Consumer<List<TopicAccessError>> onComplete) {
      if (matches.isEmpty()) {
        onComplete.accept(Collections.emptyList());
        return;
      }
      
      final AtomicInteger remainingOutcomes = new AtomicInteger(numAuthenticators);
      final List<TopicAccessError> errors = new CopyOnWriteArrayList<>();
      
      for (MatchedAuthenticators match : matches) {
        for (Authenticator authenticator : match.authenticators) {
          authenticator.verify(nexus, match.topic, new AuthenticationOutcome() {
            @Override public void allow(long millis) {
              complete();
            }
    
            @Override public void deny(TopicAccessError error) {
              errors.add(error);
              complete();
            }
            
            private void complete() {
              if (remainingOutcomes.decrementAndGet() == 0) {
                onComplete.accept(errors);
              }
            }
          });
        }
      }
    }

    @Override public String toString() {
      return "CombinedMatches [matches=" + matches + ", numAuthenticators=" + numAuthenticators + "]";
    }
  }
  
  public static final class MatchedAuthenticators {
    public final String topic;
    public final List<Authenticator> authenticators;
    
    MatchedAuthenticators(String topic, List<Authenticator> authenticators) {
      this.topic = topic;
      this.authenticators = authenticators;
    }

    @Override public String toString() {
      return "MatchedAuthenticators [topic=" + topic + ", authenticators=" + authenticators + "]";
    }
  }
  
  /**
   *  Produces the combined set of matches for the given topics.
   *  
   *  @param topics The topics.
   *  @return The combined matches.
   */
  public final CombinedMatches getMatches(Collection<String> topics) {
    if (topics.isEmpty()) return CombinedMatches.EMPTY;
    
    final List<MatchedAuthenticators> mappings = new ArrayList<>(topics.size());
    int numAuthenticators = 0;
    for (String topic : topics) {
      final List<Authenticator> authenticators = get(topic);
      mappings.add(new MatchedAuthenticators(topic, authenticators));
      numAuthenticators += authenticators.size();
    }
    return new CombinedMatches(mappings, numAuthenticators);
  }
  
  /**
   *  Similar to {@link #getMatches(Collection)}, but optimised for single-topic use.
   *  
   *  @param topic The topic.
   *  @return The matches for this topic.
   */
  public final CombinedMatches getMatches(String topic) {
    final List<Authenticator> authenticators = get(topic);
    final List<MatchedAuthenticators> mappings = Collections.singletonList(new MatchedAuthenticators(topic, authenticators));
    return new CombinedMatches(mappings, authenticators.size());
  }
  
  public static final class NoAuthenticatorException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    NoAuthenticatorException(String m) { super(m); }
  }
  
  private final Map<Topic, Authenticator> filters = new TreeMap<>(AuthChain::byLengthDescending);
  
  private static int byLengthDescending(Topic t1, Topic t2) {
    final int lengthComparison = Integer.compare(t2.length(), t1.length());
    if (lengthComparison != 0) {
      return lengthComparison;
    } else {
      return t2.toString().compareTo(t1.toString());
    }
  }
  
  protected AuthChain() {}
  
  public final Map<Topic, Authenticator> getFilters() {
    return Collections.unmodifiableMap(filters);
  }
  
  public final AuthChain<A> clear() {
    filters.clear();
    return this;
  }
  
  private Topic create(String topic) {
    return topic.isEmpty() ? Topic.root() : Topic.of(topic);
  }
  
  public final AuthChain<A> set(String topicPrefix, Authenticator authenticator) {
    filters.put(create(topicPrefix), authenticator);
    return this;
  }
  
  private static final class Match {
    static final Match INCOMPLETE = new Match(-1, true);
    
    final int length;
    final boolean definite;
    
    private Match(int length, boolean definite) {
      this.length = length;
      this.definite = definite;
    }
    
    static Match common(String[] filter, String[] topic) {
      final int extent = Math.min(filter.length, topic.length);
      
      boolean definite = true;
      int i;
      for (i = 0; i < extent; i++) {
        if (filter[i].equals(topic[i])) {
        } else if (topic[i].equals(SL_WILDCARD)) {
          definite = false;
        } else {
          break;
        }
      }
      
      if (i != filter.length && i != topic.length) return INCOMPLETE;
      
      return new Match(i, definite);
    }
    
    @Override public String toString() {
      return length != -1 ? String.format("length=%s, definite=%b", length, definite) : "length=" + length;
    }
  }
  
  private static String[] stripMLWildcard(Topic topic) {
    if (topic.isMultiLevelWildcard()) {
      return topic.subtopic(0, topic.length() - 1).getParts();
    } else {
      return topic.getParts();
    }
  }
  
  /**
   *  Obtains all authenticators applicable to the given topic (which may be an exact topic or a topic 
   *  comprising wildcards).<p>
   *   
   *  The algorithm works by iterating over each installed path filter, aggregating authenticators based
   *  on the concept of a <em>definite</em> and <em>plausible</em> matches. All plausible matches are
   *  aggregated, as well as only the greediest definite matches (of which there may be more than one).<p>
   *      
   *  The base definition of a match involves a path filter (mapped to a single authenticator) and a topic 
   *  (what the user is trying to publish/subscribe to), such that there is a number (zero or more) of 
   *  common leading segments and <em>either</em> the filter or the topic length equals to the length of 
   *  the match. In other words either the filter is completely consumed by the topic, or vice versa. In
   *  fact, the base portion of the algorithm doesn't care which is the filter and which is the topic - 
   *  the two operands may be interchanged without affecting the outcome.<p>
   *  
   *  Some examples of matching filters (on the left) and topics (on the right):<p>
   *  
   *  them them/apples<p>
   *  them them/pears<p>
   *  them them<p>
   *  them/apples them<p>
   *  them/apples them/+<p>
   *  them/apples them/#<p>
   *  them/apples +<p>
   *  
   *  On the other hand, the filter 'them/apples' will not match the topic 'them/pears', as neither 
   *  completely consumes the other.<p>
   *  
   *  Multi-level wildcards are first stripped of the trailing '#' before being subjected to the 
   *  matching process. So 'them/#' is treated as 'them', with a caveat.<p>
   *  
   *  A definite match is one where the filter matches the given topic without traversing any 
   *  wildcards. So 'them' matches 'them/apples' definitively, whereas 'them/apples' matches 
   *  'them/+' or '+/apples' plausibly.<p>
   *  
   *  When dealing with exact topics, or topics containing single-level (but not multi-level) 
   *  wildcards, a match only qualifies when the filter (left) is completely consumed by the 
   *  topic (right), which is stricter than a base match criteria (which allows for either to be 
   *  consumed by the other). So 'them' matches 'them/apples', 'them/pears' and 'them', but 
   *  'them/apples' will not match 'them'. When dealing with a multi-level wildcard, the filter 
   *  'them/apples' matches 'them/#', as well as '#'.<p>
   *  
   *  Upon completion, this method yields <em>all</em> plausible matches, as well as the (equal) 
   *  longest definite matches.<p>
   *  
   *  If no matches were found, a {@link NoAuthenticatorException} is thrown. This is an abnormal
   *  condition, indicating that the chain wasn't set up properly (i.e. no root filter was
   *  registered).
   *  
   *  @param topic The topic under consideration.
   *  @return The matching authenticators.
   *  @exception NoAuthenticatorException If no matches were found.
   */
  public final List<Authenticator> get(String topic) {
    final Topic original = Topic.of(topic);
    final String[] stripped = stripMLWildcard(original);
    final boolean exactOrSL = stripped.length == original.length();
    if (LOG.isTraceEnabled()) LOG.trace("topic={} ({}), stripped={} ({}), exactOrSL={}", 
                                        original, original.length(), Arrays.toString(stripped), 
                                        stripped.length, exactOrSL);
    
    final List<Authenticator> definite = new ArrayList<>();
    final List<Authenticator> plausible = new ArrayList<>();
    int longestDefiniteMatch = 0;
    for (Map.Entry<Topic, Authenticator> entry : filters.entrySet()) {
      final Topic filter = entry.getKey();
      final Match match = Match.common(filter.getParts(), stripped);
      if (LOG.isTraceEnabled()) LOG.trace("  filter={}, match: {}", filter, match);
      if (match == Match.INCOMPLETE || exactOrSL && match.length != filter.length()) continue;
      
      if (match.definite && match.length >= longestDefiniteMatch) {
        longestDefiniteMatch = match.length;
        definite.add(entry.getValue());
        if (LOG.isTraceEnabled()) LOG.trace("    adding definite {}", filter);
      } else if (! match.definite) {
        plausible.add(entry.getValue());
        if (LOG.isTraceEnabled()) LOG.trace("    adding plausible {}", filter);
      }
    }
    
    if (definite.isEmpty() && plausible.isEmpty()) {
      throw new NoAuthenticatorException("No match for topic " + topic + ", filters=" + filters.keySet());
    }
    
    final List<Authenticator> all = definite;
    all.addAll(plausible);
    return all;
  }
  
  public final void validate() {
    get("#");
  }
  
  @Override
  public final void close() throws Exception {
    for (Authenticator auth : filters.values()) {
      auth.close();
    }
  }
}
