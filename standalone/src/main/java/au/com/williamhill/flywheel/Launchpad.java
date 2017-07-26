package au.com.williamhill.flywheel;

import static java.lang.System.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

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
    if (! profileYaml.exists()) {
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
    
    sb.append("\n  Launchers:");
    for (Launcher launcher : profile.launchers) {
      sb.append("\n    ").append(launcher);
    }
    
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
        throw new LaunchpadException("Failed to launch " + launcher, getUltimateCause(e));
      }
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
    ShInteractor.Ulimit.main(null);
    try {
      new Launchpad(getProfilePath(System.getenv())).launch(args);
    } catch (LaunchpadException e) {
      err.format("Error:\n");
      e.printStackTrace(err);
      System.exit(1);
    }
  }
}

final class ShInteractor {
  private ShInteractor() {}
  
  public static int execute(String command, boolean waitForResponse, Consumer<String> handler) {
    int shellExitStatus = -1;
    final ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
    pb.redirectErrorStream(true);
    try {
      final Process shell = pb.start();

      if (waitForResponse) {
        final InputStream shellIn = shell.getInputStream();
        shellExitStatus = shell.waitFor();
        convertStreamToStr(shellIn, handler);
        shellIn.close();
      }
    } catch (IOException e) {
      System.err.println("Error occured while executing command: " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return shellExitStatus;
  }

  private static String convertStreamToStr(InputStream is, Consumer<String> handler) throws IOException {
    if (is != null) {
      final Writer writer = new StringWriter();
      final char[] buffer = new char[1024];
      try {
        final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        int n;
        while ((n = reader.read(buffer)) != -1) {
          final String output = new String(buffer, 0, n);
          writer.write(buffer, 0, n);

          if (handler != null) {
            handler.accept(output);
          }
        }
      } finally {
        is.close();
      }
      return writer.toString();
    } else {
      return "";
    }
  }

  public static final class Ulimit {
    public static void main(String[] args) {
      System.out.println("$ ulimit -Sa");
      ShInteractor.execute("ulimit -Sa", true, System.out::print);
    }
  }
}
