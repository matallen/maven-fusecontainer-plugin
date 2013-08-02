package org.jboss.fuse.maven;

import static com.jayway.restassured.RestAssured.with;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.IOUtils;

import com.jayway.restassured.response.Response;

public class FuseContainer {
  public static FuseContainer instance=null;
  private static String PATH=null;
  private static String clientUsername;
  private static String clientPassword;
  private static Thread t;
  private static boolean debug=false;
  
  public void stop(Configuration config) throws InterruptedException, IOException{
    setExit(true);
    t.stop();
  }
  
  public void start(final Configuration config) throws InterruptedException{
    try{
      System.setProperty("PLUGIN_FUSE_HOME", config.getFuseHome());
      PATH=config.getFuseHome();
      clientUsername=config.getClientUsername();
      clientPassword=config.getClientPassword();
      debug=config.getDebug();
      
      t=startContainer(config.getFuseHome()+"/bin/karaf");
      t.start();
      
      int waitForActiveBundles=15;
      Wait.For(waitForActiveBundles, new ToHappen() {public boolean hasHappened(){
        return executeCommand("osgi:list | grep Active").length()>0;
      }}, "osgi:list | grep Active");
      
      Wait.For(config.getTimeout()-waitForActiveBundles, new ToHappen() {public boolean hasHappened(){
        return executeCommand("osgi:list | grep Resolved").length()==0;
      }}, "osgi:list | grep Resolved");
      
      if (null!=config.getInstallScript()){
        if (debug) System.out.println("FOUND INSTALLER SCRIPT = "+config.getInstallScript());
        
        executeCommand(config.getInstallScript());
        
        Wait.For(config.getInstallScriptTimeout(), new ToHappen() {public boolean hasHappened(){
          return executeCommand("features:list | grep Installed").length()==0;
        }}, "features:list | grep Installed");
      }else{
        if (debug) System.out.println("NO INSTALLER SCRIPT FOUND");
      }
      
    }catch (Exception e){
      e.printStackTrace();
    }
  }
  
  public String executeCommand(String command){
    try{
//      System.out.println("EXECUTING: "+command);
      Process p=Runtime.getRuntime().exec(new String[]{PATH+"/bin/client","-u",clientUsername,"-p",clientPassword, command});
      String result=IOUtils.toString(p.getInputStream());
      if (debug) System.out.println("result of '"+command+"'\n"+result);
      return result;
    }catch(Exception sink){
      return null;
    }
  }
  private boolean exit=false;
  public synchronized void setExit(boolean value){this.exit=value;};
  public Thread startContainer(String command) throws IOException{
    final Process child=Runtime.getRuntime().exec(new String[]{command});
    Runnable r=new Runnable() {
      byte[] buf=new byte[256];
      public void run() {
        try {
          while(!exit){
            child.getInputStream().read(buf);
            System.out.print(new String(buf));
            buf=new byte[256];
          }
          if (debug) System.out.println("EXITING due to property exit==true!!!");
        } catch (IOException e) {e.printStackTrace(); }
      }
    };
    return new Thread(r);
  }
}
