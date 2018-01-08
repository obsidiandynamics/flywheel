package au.com.williamhill.flywheel;

import static java.lang.System.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.slf4j.*;
import org.slf4j.Logger;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.util.*;
import au.com.williamhill.flywheel.util.Exceptions.ThrowingRunnable;
import au.com.williamhill.flywheel.util.Exceptions.ThrowingSupplier;

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
    if (! profileYaml.exists()) {
      throw new LaunchpadException("Profile configuration " + profileYaml + " is missing", null);
    }

    profile = rethrowLaunchpadException(() -> {
      return Profile.fromFile(profileYaml);
    }, "Error reading profile");
    
    final StringBuilder sb = new StringBuilder();
    
    rethrowLaunchpadException(() -> {
      sb.append("\n  Flywheel version: ").append(FlywheelVersion.get());
      sb.append("\n  Indigo version: ").append(IndigoVersion.get());
    }, "Error retrieving version"); 
    
    sb.append("\n  Properties:");
    for (Map.Entry<String, ?> entry : profile.properties.entrySet()) {
      sb.append("\n    ").append(entry.getKey()).append(": ").append(entry.getValue());
    }
    
    sb.append("\n  Launchers:");
    for (Launcher launcher : profile.launchers) {
      sb.append("\n    ").append(launcher);
    }
    
    final Logger log = LoggerFactory.getLogger(Launchpad.class);
    log.info(sb.toString());
  }
  
  private static void rethrowLaunchpadException(ThrowingRunnable lambda, String message) throws LaunchpadException {
    rethrowLaunchpadException((ThrowingSupplier<Void>) lambda, message);
  }
  
  private static <T> T rethrowLaunchpadException(ThrowingSupplier<T> lambda, String message) throws LaunchpadException {
    return Exceptions.rethrow(lambda, e -> new LaunchpadException(message, Exceptions.getRootCause(e)));
  }
  
  public void launch(String[] args) throws LaunchpadException {
    for (Launcher launcher : profile.launchers) {
      rethrowLaunchpadException(() -> launcher.launch(args), "Failed to launch " + launcher);
    }
  }
  
  public Profile getProfile() {
    return profile;
  }
  
  static File getProfilePath(Map<String, String> env) {
    final String propertyName = "flywheel.launchpad.profile";
    final String envName = "FLYWHEEL_PROFILE";
    
    final String profileProp = System.getProperty(propertyName);
    final File profilePath;
    if (profileProp != null) {
      profilePath = new File(profileProp);
    } else {
      final String profileEnv = env.get(envName);
      if (profileEnv != null) {
        profilePath = new File(profileEnv);
      } else {
        profilePath = new File(DEF_PROFILE);
      }
    }
    return profilePath;
  }
  
  public static void main(String... args) {
    bootstrap(args, System::exit);
  }
  
  static void bootstrap(String[] args, IntConsumer exitHandler) {
    try {
      new Launchpad(getProfilePath(System.getenv())).launch(args);
    } catch (LaunchpadException e) {
      err.format("Error:\n");
      e.printStackTrace(err);
      exitHandler.accept(1);
    }
  }
}
