package org.jboss.fuse.maven;

public class Main {

  public static void main(String[] args) throws Exception {
    new FuseContainer().start(new Configuration() {
      public boolean getSkip() {
        return false;
      }
      public String getFuseHome() {
        return "/home/mallen/jboss-fuse-6.0.0.redhat-024";
      }
      public boolean getDebug() {
        return true;
      }
      public int getTimeout() {
        return 20;
      }
      public String getClientUsername() {
        return "admin";
      }
      public String getClientPassword() {
        return "admin";
      }
      public String getInstallScript() {
        return "shell:source mvn:com.truphone.esb/features/1.0.0-SNAPSHOT/karaf/installer;";
      }
      public int getInstallScriptTimeout() {
        return 30;
      }
    });
  }
}
