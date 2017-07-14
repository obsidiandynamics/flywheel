package au.com.williamhill.flywheel;

import static java.lang.System.*;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import au.com.williamhill.flywheel.yconf.*;

public final class Launchpad {
  static final class LauncherException extends Exception {
    private static final long serialVersionUID = 1L;
    
    LauncherException(String m, Throwable cause) { super(m, cause); }
  }
  
  Launchpad(File profilePath) throws LauncherException {
    if (! profilePath.exists()) {
      throw new LauncherException("Profile path " + profilePath + " does not exist", null);
    }
    
    if (! profilePath.isDirectory()) {
      throw new LauncherException("Profile path must be a directory", null);
    }
    
    final File profileYaml = new File(profilePath.getPath() + "/profile.yaml");
    if (! profileYaml.exists() || ! profileYaml.isFile()) {
      throw new LauncherException("Profile configuration " + profileYaml + " is missing", null);
    }
    
    final Profile p;
    try {
      p = Profile.fromFile(profileYaml);
    } catch (Exception e) {
      throw new LauncherException("Error reading profile", e);
    }
    
    final StringBuilder sb = new StringBuilder("\nproperties:");
    for (Map.Entry<String, ?> entry : p.properties.entrySet()) {
      sb.append("\n  ").append(entry.getKey()).append(": ").append(entry.getValue());
      final String unmasked = Masked.unmask(entry.getValue());
      System.setProperty(entry.getKey(), unmasked);
    }
    
    sb.append("\nlauncher: ").append(p.launcher);
    
    final Logger log = LoggerFactory.getLogger(Launchpad.class);
    log.info(sb.toString());
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
        err.format("Error: no profile name specified.\n");
        err.format("Point either the %s system property or the %s environment variable to the profile directory.\n",
                   propertyName, envName);
        System.exit(1);
        return;
      }
    }
    
    try {
      new Launchpad(profilePath);
    } catch (LauncherException e) {
      err.format("Error: " + e);
      e.printStackTrace(err);
      System.exit(1);;
    }
  }
}
