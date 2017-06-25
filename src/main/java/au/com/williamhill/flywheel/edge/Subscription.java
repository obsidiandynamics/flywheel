package au.com.williamhill.flywheel.edge;

import java.util.*;

@FunctionalInterface
interface Subscription {
  Set<String> getTopics();
}
