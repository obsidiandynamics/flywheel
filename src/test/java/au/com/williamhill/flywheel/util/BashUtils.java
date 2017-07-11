package au.com.williamhill.flywheel.util;

import java.util.concurrent.atomic.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.util.DockerCompose.*;

/**
 *  Utilities supporting Bash command execution; an extension of Indigo's {@link BashInteractor}.
 */
public final class BashUtils {
  private static final String PATH = PropertyUtils.get("flywheel.bash.path", String::valueOf, null);
  
  private BashUtils() {}
  
  public static void checkInstalled(String name, String verifyCmd) {
    final StringBuilder out = new StringBuilder();
    final int code = BashInteractor.execute(commandWithPath(verifyCmd), true, out::append);
    if (code == 127) {
      final AtomicReference<String> path = new AtomicReference<>();
      BashInteractor.execute(commandWithPath("echo $PATH"), true, path::set);
      throw new AssertionError(name + " is not installed, or is missing from the path (" + path + ")");
    } else if (code != 0) {
      throw new AssertionError("Error running " + name + ": " + out);
    }
  }
  
  public static void run(CharSequence cmd) throws DockerException {
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
