package au.com.williamhill.flywheel.util;

import java.util.concurrent.atomic.*;

import com.obsidiandynamics.indigo.util.*;

public final class DockerUtils {
  private static final String PATH = PropertyUtils.get("flywheel.bash.path", String::valueOf, null);
  
  private DockerUtils() {}
  
  public static final class DockerException extends Exception {
    private static final long serialVersionUID = 1L;
    
    DockerException(String command, String output, int code) { 
      super(String.format("Command '%s' terminated with code %d, output: %s", command, code, output)); 
    }
  }
  
  public static void checkInstalled() {
    final int code = BashInteractor.execute(commandWithPath("docker"), true, s -> {});
    if (code != 0) {
      final AtomicReference<String> path = new AtomicReference<>();
      BashInteractor.execute(commandWithPath("echo $PATH"), true, path::set);
      throw new AssertionError("Docker is not installed, or is missing from the path (" + path + ")");
    }
  }
  
  public static void composeRemove(String file) throws DockerException {
    final String command = String.format("docker-compose -f %s rm", file);
    run(command);
  }
  
  public static void composeUp(String file) throws DockerException {
    final String command = String.format("docker-compose -f %s up -d", file);
    run(command);
  }
  
  public static void composeDown(String file) throws DockerException {
    final String command = String.format("docker-compose -f %s down -v", file);
    run(command);
  }
  
  public static void composeStop(String file, int timeout) throws DockerException {
    final String command = String.format("docker-compose -f %s stop -t %d", file, timeout);
    run(command);
  }
  
  private static void run(String command) throws DockerException {
    final String commandWithPath = commandWithPath(command);
    final StringBuilder out = new StringBuilder();
    final int code = BashInteractor.execute(commandWithPath, true, out::append);
    if (code != 0) {
      throw new DockerException(commandWithPath, out.toString(), code);
    }
  }
  
  private static String commandWithPath(String command) {
    final StringBuilder sb = new StringBuilder();
    if (PATH != null) {
      sb.append("export PATH=").append(PATH).append(" && ");
    }
    sb.append(command);
    return sb.toString();
  }
}
