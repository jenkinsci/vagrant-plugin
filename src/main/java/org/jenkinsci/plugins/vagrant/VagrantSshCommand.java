package org.jenkinsci.plugins.vagrant;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by elad on 10/13/14.
 */
public class VagrantSshCommand extends Builder {

  private String command;
  private boolean asRoot;
  private VagrantWrapper wrapper;

  @DataBoundConstructor
  public VagrantSshCommand(String vagrantFile, String vagrantVm, String command, boolean asRoot) {
    this.wrapper = new VagrantWrapper(vagrantFile, vagrantVm);
    this.command = command;
    this.asRoot = asRoot;
  }

  /**
   *
   * @return
   */
  public String getCommand() {
    return command;
  }

  /**
   *
   * @param command
   */
  public void setCommand(String command) {
    this.command = command;
  }

  /**
   *
   * @return
   */
  public boolean isAsRoot() {
    return asRoot;
  }

  /**
   *
   * @param asRoot
   */
  public void setAsRoot(boolean asRoot) {
    this.asRoot = asRoot;
  }

  public String getVagrantFile() {
    return wrapper.getVagrantFile();
  }

  public String getVagrantVm() {
    return this.wrapper.getVagrantVm();
  }

  @Extension
  public static final VagrantSshCommandDescriptor DESCRIPTOR = new VagrantSshCommandDescriptor();

  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    this.wrapper.setBuild(build);
    this.wrapper.setLauncher(launcher);
    this.wrapper.setListener(listener);
    try {

        String[] commands = this.command.split(System.lineSeparator());

        boolean result = true;

        for (String com : commands) {

            if (com.startsWith("#")) {
                continue;
            }

            List<String> arg = new ArrayList<String>();

            arg.add("--command");
            if (this.asRoot) {
                arg.add("sudo " + com);
            } else {
                arg.add(com);
            }

            result = result && this.wrapper.executeCommand("ssh", arg);

            if (!result) {
                return result;
            }
        }
        return result;
    } catch (IOException e) {
      wrapper.log("Error starting up vagrant, caught IOException, message: " + e.getMessage());
      wrapper.log(e.getStackTrace());
      return false;
    } catch (InterruptedException e) {
      wrapper.log("Error starting up vagrant, caught InterruptedException, message: " + e.getMessage());
      wrapper.log(e.getStackTrace());
      return false;
    }
  }


  @Override
  public Descriptor<Builder> getDescriptor() {
    return DESCRIPTOR;
  }

  public static final class VagrantSshCommandDescriptor extends Descriptor<Builder> {

    public String getDisplayName() {
      return "Run a command in vagrant machine";
    }

    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      // Indicates that this builder can be used with all kinds of project types
      return true;
    }
  }

}
