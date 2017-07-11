package au.com.williamhill.flywheel.logging;

import java.text.*;
import java.util.*;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

import com.google.gson.*;

/**
 *  Layout for JSON logging.<p>
 *  
 *  Adapted from https://github.com/michaeltandy/log4j-json.
 */
public final class JsonLayout extends Layout {
  private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
  private final String hostname = getHostname().toLowerCase();
  private final String username = System.getProperty("user.name").toLowerCase();
  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private Level minimumLevelForSlowLogging = Level.ALL;
  private String mdcRoot;
  private List<String> mdcFieldsToLog = Collections.emptyList();

  @Override
  public String format(LoggingEvent le) {
    final Map<String, Object> r = new LinkedHashMap<>();
    r.put("timestamp", dateFormat.format(new Date(le.timeStamp)));
    r.put("host", hostname);
    r.put("user", username);
    r.put("level", le.getLevel().toString());
    r.put("thread", le.getThreadName());
    r.put("ndc",le.getNDC());
    if (le.getLevel().isGreaterOrEqual(minimumLevelForSlowLogging)) {
      r.put("class", le.getLocationInformation().getClassName());
      r.put("line", safeParseInt(le.getLocationInformation().getLineNumber()));
      r.put("method", le.getLocationInformation().getMethodName());
    }
    r.put("message", safeToString(le.getMessage()));
    r.put("throwable", formatThrowable(le) );

    for (String mdcKey : mdcFieldsToLog) {
      if (! r.containsKey(mdcKey)) {
        r.put(mdcKey, safeToString(le.getMDC(mdcKey)));
      }
    }
    
    if (mdcRoot != null) {
      final Object mdcValue = le.getMDC(mdcRoot);
      if (mdcValue != null) {
        final String[] fields = ((String) mdcValue).split(",");
        for (String field : fields) {
          final String trimmedField = field.trim();
          r.put(trimmedField, safeToString(le.getMDC(trimmedField)));
        }
      }
    }

    after(le, r);
    return gson.toJson(r) + "\n";
  }

  /**
   *  Method called near the end of formatting a LoggingEvent in case users
   *  want to override the default object fields. 
   *  
   *  @param le The event being logged.
   *  @param r The map which will be output.
   */
  public void after(LoggingEvent le, Map<String,Object> r) {}

  /**
   *  LoggingEvent messages can have any type, and we call toString on them. As
   *  the user can define the <code>toString</code> method, we should catch any exceptions.
   *  
   *  @param obj The object to parse.
   *  @return The string value.
   */
  private static String safeToString(Object obj) {
    if (obj == null) return null;
    try {
      return obj.toString();
    } catch (Throwable t) {
      return "Error getting message: " + t.getMessage();
    }
  }

  /**
   *  Safe integer parser, for when line numbers aren't available. See for
   *  example https://github.com/michaeltandy/log4j-json/issues/1
   * 
   *  @param obj The object to parse.
   *  @return The int value
   */
  private static Integer safeParseInt(String obj) {
    try {
      return Integer.parseInt(obj.toString());
    } catch (NumberFormatException t) {
      return null;
    }
  }

  /**
   *  If a throwable is present, format it with newlines between stack trace
   *  elements. Otherwise return null.
   *  
   *  @param le The logging event.
   */
  private String formatThrowable(LoggingEvent le) {
    if (le.getThrowableInformation() == null || 
        le.getThrowableInformation().getThrowable() == null)
      return null;

    return mkString(le.getThrowableStrRep(), "\n");
  }

  private String mkString(Object[] parts,String separator) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; ; i++) {
      sb.append(parts[i]);
      if (i == parts.length - 1)
        return sb.toString();
      sb.append(separator);
    }
  }

  @Override
  public boolean ignoresThrowable() {
    return false;
  }

  @Override
  public void activateOptions() {}

  private static String getHostname() {
    String hostname;
    try {
      hostname = java.net.InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      hostname = "Unknown, " + e.getMessage();
    }
    return hostname;
  }

  public void setMinimumLevelForSlowLogging(String level) {
    minimumLevelForSlowLogging = Level.toLevel(level, Level.ALL);
  }
  
  public void setMdcRoot(String mdcRoot) {
    this.mdcRoot = mdcRoot;
  }

  public void setMdcFieldsToLog(String toLog) {
    if (toLog == null || toLog.isEmpty()) {
      mdcFieldsToLog = Collections.emptyList();
    } else {
      final ArrayList<String> listToLog = new ArrayList<>();
      for (String token : toLog.split(",")) {
        token = token.trim();
        if (! token.isEmpty()) {
          listToLog.add(token);
        }
      }
      mdcFieldsToLog = Collections.unmodifiableList(listToLog);
    }
  }
}
