package au.com.williamhill.flywheel.util;

/**
 *  An interface to Docker Compose, equivalent of BashUtils.running the 'docker-compose' CLI.
 */
public final class DockerCompose {
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
    BashUtils.checkInstalled("Docker Compose", "docker-compose version");
  }
  
  public void up() throws DockerException {
    final CharSequence cmd = new StringChainer("docker-compose")
        .when(project != null).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" up -d");
    BashUtils.run(cmd);
  }
  
  public void down(boolean removeVolumes) throws DockerException {
    final CharSequence cmd = new StringChainer("docker-compose")
        .when(project != null).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" down")
        .when(removeVolumes).append(" -v");
    BashUtils.run(cmd);
  }
  
  public void stop(int timeout) throws DockerException {
    final CharSequence cmd = new StringChainer("docker-compose")
        .when(project != null).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" stop")
        .when(timeout != 0).append(new StringBuilder(" -t ").append(timeout));
    BashUtils.run(cmd);
  }
  
  public void rm(boolean removeVolumes) throws DockerException {
    final CharSequence cmd = new StringChainer("docker-compose")
        .when(project != null).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" rm -f")
        .when(removeVolumes).append(" -v");
    BashUtils.run(cmd);
  }
}
