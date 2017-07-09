package au.com.williamhill.flywheel.util;

import java.util.concurrent.atomic.*;

import com.obsidiandynamics.indigo.util.*;

public final class DockerCompose {
  private static final String PATH = PropertyUtils.get("flywheel.bash.path", String::valueOf, null);
  
  private final String project;
  
  private final String composeFile;
  
  public DockerCompose(String project, String composeFile) {
    if (composeFile == null) throw new IllegalArgumentException("The compose file cannot be null");
    this.project = project;
    this.composeFile = composeFile;
  }
  
  public static final class DockerException extends Exception {
    private static final long serialVersionUID = 1L;
    
    DockerException(String command, String output, int code) { 
      super(String.format("Command '%s' terminated with code %d, output: %s", command, code, output)); 
    }
  }
  
  public static void checkInstalled() {
    final int code = BashInteractor.execute(commandWithPath("docker-compose version"), true, s -> {});
    if (code != 0) {
      final AtomicReference<String> path = new AtomicReference<>();
      BashInteractor.execute(commandWithPath("echo $PATH"), true, path::set);
      throw new AssertionError("Docker Compose is not installed, or is missing from the path (" + path + ")");
    }
  }
  
  public void up() throws DockerException {
    final CharSequence cmd = new StringChainer("docker-compose")
        .when(project != null).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" up -d");
    run(cmd);
  }
  
  public void down(boolean removeVolumes) throws DockerException {
    final CharSequence cmd = new StringChainer("docker-compose")
        .when(project != null).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" down")
        .when(removeVolumes).append(" -v");
    run(cmd);
  }
  
  public void stop(int timeout) throws DockerException {
    final CharSequence cmd = new StringChainer("docker-compose")
        .when(project != null).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" stop")
        .when(timeout != 0).append(new StringBuilder(" -t ").append(timeout));
    run(cmd);
  }
  
  public void rm(boolean removeVolumes) throws DockerException {
    final CharSequence cmd = new StringChainer("docker-compose")
        .when(project != null).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" rm -f")
        .when(removeVolumes).append(" -v");
    run(cmd);
  }
  
  private static void run(CharSequence cmd) throws DockerException {
    final String commandWithPath = commandWithPath(cmd);
    final StringBuilder out = new StringBuilder();
    final int code = BashInteractor.execute(commandWithPath, true, out::append);
    if (code != 0) {
      throw new DockerException(commandWithPath, out.toString(), code);
    }
  }
  
  private static String commandWithPath(CharSequence cmd) {
    return new StringChainer()
        .when(PATH != null).append(new StringBuilder("export PATH=").append(PATH).append(" && "))
        .append(cmd).toString();
  }
}
