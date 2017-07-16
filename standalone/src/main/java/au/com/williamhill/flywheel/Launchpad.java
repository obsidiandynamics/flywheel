package au.com.williamhill.flywheel;

import static java.lang.System.*;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.indigo.util.*;

public final class Launchpad {
  private static final String DEF_PROFILE = "conf/default";
  
  static final class LaunchpadException extends Exception {
    private static final long serialVersionUID = 1L;
    
    LaunchpadException(String m, Throwable cause) { super(m, cause); }
  }
  
  private final Profile profile;
  
  Launchpad(File profilePath) throws LaunchpadException {
    if (! profilePath.exists()) {
      throw new LaunchpadException("Profile path " + profilePath + " does not exist", null);
    }
    
    if (! profilePath.isDirectory()) {
      throw new LaunchpadException("Profile path must be a directory", null);
    }
    
    final File profileYaml = new File(profilePath.getPath() + "/profile.yaml");
    if (! profileYaml.exists() || ! profileYaml.isFile()) {
      throw new LaunchpadException("Profile configuration " + profileYaml + " is missing", null);
    }
    
    try {
      profile = Profile.fromFile(profileYaml);
    } catch (Exception e) {
      throw new LaunchpadException("Error reading profile", getUltimateCause(e));
    }
    
    final StringBuilder sb = new StringBuilder();
    
    try {
      sb.append("\n  Flywheel version: ").append(FlywheelVersion.get());
      sb.append("\n  Indigo version: ").append(IndigoVersion.get());
    } catch (IOException e) {
      throw new LaunchpadException("Error retrieving version", e);
    }
    
    sb.append("\n  Properties:");
    for (Map.Entry<String, ?> entry : profile.properties.entrySet()) {
      sb.append("\n    ").append(entry.getKey()).append(": ").append(entry.getValue());
    }
    
    sb.append("\n  Launcher: ").append(profile.launchers);
    
    final Logger log = LoggerFactory.getLogger(Launchpad.class);
    log.info(sb.toString());
  }
  
  private static Throwable getUltimateCause(Throwable throwable) {
    Throwable cause = throwable;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }
  
  public void launch(String[] args) throws LaunchpadException {
    for (Launcher launcher : profile.launchers) {
      try {
        launcher.launch(args);
      } catch (Exception e) {
        throw new LaunchpadException("Failed to launch " + launcher, e);
      }
    }
  }
  
  public Profile getProfile() {
    return profile;
  }
  
  public static void main(String[] args) {
    final String propertyName = "flywheel.launchpad.profile";
    final String envName = "FLYWHEEL_PROFILE";
    
    final String profileProp = System.getProperty(propertyName);
    final File profilePath;
    if (profileProp != null) {
      profilePath = new File(profileProp);
    } else {
      final String profileEnv = System.getenv(envName);
      if (profileEnv != null) {
        profilePath = new File(profileEnv);
      } else {
        profilePath = new File(DEF_PROFILE);
      }
    }
    
    try {
      new Launchpad(profilePath).launch(args);
    } catch (LaunchpadException e) {
      err.format("Error:\n");
      e.printStackTrace(err);
      System.exit(1);;
    }
  }
}
