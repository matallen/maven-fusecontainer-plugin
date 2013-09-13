package org.jboss.fuse.maven;

import static com.jayway.restassured.RestAssured.with;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;

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
      PATH=config.getFuseHome();
      clientUsername=config.getClientUsername();
      clientPassword=config.getClientPassword();
      debug=config.getDebug();
      
      // check PATH+"/bin/client" exists and is executable
      File clientFile=new File(PATH+"/bin/client");
      if (!clientFile.exists()) throw new RuntimeException(PATH+"/bin/client does not exist! aborting start of container!");
      clientFile.setExecutable(true, false);
      
      File karafFile=new File(PATH+"/bin/karaf");
      if (!karafFile.exists()) throw new RuntimeException(PATH+"/bin/karaf does not exist! aborting start of container!");
      karafFile.setExecutable(true, false);
      
      // TODO: check PATH+"/etc/users.properties contains admin=admin,admin"
//      String usersPropertiesContent=IOUtils.toString(new FileInputStream(new File(PATH+"/etc/users.properties")));
//      if (!usersPropertiesContent.contains("admin=admin,admin") && usersPropertiesContent.contains("#admin=admin,admin")){
//        usersPropertiesContent+="\nadmin=admin,admin";
//        System.err.println("appending admin=admin,admin to users.properties");
//        //TODO: write it
//      }else{
//        System.out.println("not appending user information");
//      }
      
      
      t=startContainer(config, config.getFuseHome()+"/bin/karaf");
      t.start();
      
      int waitForActiveBundles=15;
      Wait.For(waitForActiveBundles, new ToHappen() {public boolean hasHappened(){
        return executeCommand("osgi:list | grep Active").length()>0;
      }}, "osgi:list | grep Active");
      
      Wait.For(config.getTimeout()-waitForActiveBundles, new ToHappen() {public boolean hasHappened(){
        return executeCommand("osgi:list | grep Resolved").length()==0;
      }}, "osgi:list | grep Resolved");
      String resolved=executeCommand("osgi:list | grep Resolved");
      if (resolved.length()>0){
        System.err.println("Following bundles did not start:\n"+resolved);
        throw new RuntimeException("Some bundles did not start within the timeout time");
      }
      
      if (null!=config.getCommands()){
        if (debug) System.out.println("FOUND COMMANDS LIST = "+config.getCommands());
        
        for(String command:config.getCommands()){
          executeCommand(command);
          Wait.For(30, new ToHappen() {public boolean hasHappened(){
//          return executeCommand("features:list | grep \"[installed\"").length()==0;
            return executeCommand("osgi:list | grep Resolved").length()==0;
          }}, "error executing command '"+command+"'");
          if (debug) System.out.println("FINISHED COMMAND");
        }
        
      }else{
        if (debug) System.out.println("NO COMMANDS FOUND");
      }
      
    }catch (Exception e){
      e.printStackTrace();
    }
  }
  
  public String executeCommand(String command){
    try{
//      System.out.println("EXECUTING: "+command);
      if (debug) System.out.println("EXECUTING '"+command+"'");
      Process p=Runtime.getRuntime().exec(new String[]{PATH+"/bin/client","-u",clientUsername,"-p",clientPassword, command});
      String result=IOUtils.toString(p.getInputStream());
      if (debug) System.out.println("RESULT(length="+result.length()+")=\n"+result);
      return result;
    }catch(Exception sink){
      return null;
    }
  }
  
  private void deleteDir(File dir) throws IOException{
    if (debug) System.out.println("Deleting dir ["+dir.getCanonicalPath()+"]");
    dir.delete();
  }
  
  private boolean exit=false;
  public synchronized void setExit(boolean value){this.exit=value;};
  public Thread startContainer(Configuration config, String command) throws IOException{
    String[] cmd=config.getClean()?new String[]{command, "--clean"}:new String[]{command};
    if (config.getClean()){
      // delete the data + instances folder since the --clean parameter doesn't
      deleteDir(new File(config.getFuseHome()+ File.separator+"/data"));
      deleteDir(new File(config.getFuseHome()+ File.separator+"/instances"));
    }
    
    final Process child=Runtime.getRuntime().exec(cmd);
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
