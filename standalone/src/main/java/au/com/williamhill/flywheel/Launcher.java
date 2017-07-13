package au.com.williamhill.flywheel;

import static java.lang.System.*;

import java.io.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.yconf.*;

public final class Launcher {
  static final class LauncherException extends Exception {
    private static final long serialVersionUID = 1L;
    
    LauncherException(String m, Throwable cause) { super(m, cause); }
  }
  
  Launcher(File profilePath) throws LauncherException {
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
  }
  
  public static void main(String[] args) {
    final String propertyName = "flywheel.launcher.profile";
    final String envName = "FLYWHEEL_PROFILE";
    
    final String profileProp = System.getProperty(propertyName);
    final File profilePath;
    if (profileProp != null) {
      profilePath = new File(profileProp);
    } else {
      final String profileEnv = System.getenv(envName);
      if (profileEnv != null) {
        profilePath = new File(profileProp);
      } else {
        err.format("Error: no profile name specified.\n");
        err.format("Set either the %s system property or the %s environment variable to point to the profile path.\n",
                   propertyName, envName);
        System.exit(1);
        return;
      }
    }
    
    try {
      new Launcher(profilePath);
    } catch (LauncherException e) {
      err.format("Error: " + e);
      e.printStackTrace(err);
      System.exit(1);;
    }
  }
}
