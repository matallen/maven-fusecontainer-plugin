package org.jboss.fuse.maven;

import java.util.HashMap;
import java.util.Map;


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
      public Map<String,String> getProperties(){
        return new HashMap<String, String>();
      }
      public String[] getCommands(){
        return new String[]{"shell:source mvn:com.redhat.example/features/1.0-SNAPSHOT/karaf/installer;"};
      }
      public boolean getClean() {
        return false;
      }
    });
  }
}
