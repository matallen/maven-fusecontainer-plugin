package org.jboss.fuse.maven.obsolete;

import static com.jayway.restassured.RestAssured.with;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.IOUtils;
import org.jboss.fuse.maven.Configuration;
import org.jboss.fuse.maven.ToHappen;
import org.jboss.fuse.maven.Wait;

import com.jayway.restassured.response.Response;

public class FuseContainerClassic {
  public static FuseContainerClassic fuseContainer=null;
  
//  private static final DecimalFormat df=new DecimalFormat("00");
//  private static final String PATH="/home/mallen/Work/19578-truphone/jboss-fuse-6.0.0.redhat-024-minimal/bin";
//  private static String[] CLIENT_SCRIPT=new String[]{PATH+"/client","-u","admin","-p","admin", "$COMMAND"};
  private static String PATH=null;
  private static Thread t;
  public void stop(Configuration config) throws InterruptedException, IOException{
//    System.out.println("YYY EXECUTING (exit)");
//    executeCommand("osgi:shutdown");
//    executeCommand("exit");
//    System.out.println("YYY COMPLETED (exit)");
    t.stop();
//    System.out.println("YYY COMPLETED t.stop()");
//    Thread.sleep(5000l);
  }
  
  public void start(Configuration config) throws InterruptedException{
    try{
      System.setProperty("PLUGIN_FUSE_HOME", config.getFuseHome());
      PATH=config.getFuseHome();
      t=startContainer(config.getFuseHome()+"/bin/karaf");
      t.start();
      
//      System.out.println("XXX WAITING FOR CONTAINER START (osgi:list | grep Active | <0)");
      
      Wait.For(10, new ToHappen() {public boolean hasHappened(){
        return executeCommand("osgi:list | grep Active").length()>0;
      }});
      
//      System.out.println("XXX WAITING FOR CONTAINER START (osgi:list | grep Resolved | ==0)");
      
      Wait.For(10, new ToHappen() {public boolean hasHappened(){
        return executeCommand("osgi:list | grep Resolved").length()==0;
      }});
      
//      String osgiListResult=executeCommand("osgi:list");
//      System.out.println("osgiListResult = "+osgiListResult);
//      executeCommand("osgi:shutdown");
      System.out.println("\nXXX STARTED!");
    }catch (Exception e){
      e.printStackTrace();
    }
  }
  
  public String executeCommand(String command){
    try{
      String result="";
      String[] commandArray=new String[]{PATH+"/bin/client","-u","admin","-p","admin", "$COMMAND"};
      commandArray[commandArray.length-1]=command;
      Process p=Runtime.getRuntime().exec(commandArray);
      result=IOUtils.toString(p.getInputStream());
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
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
//    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//      public void run() {
//        executeCommand("exit");
//        try {
//          Thread.sleep(5000l);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }
//    }));
    return new Thread(r);
  }
  
  
//  public static void waitHttp(Configuration config)throws InterruptedException{
//    for (int i = 0; i < config.getSecondsToWait(); i++) {
//      String s=null;
//      try{
//        Response response=with().get(config.getPingUrl());
//        s=response.getStatusLine();
//        if (config.getDebug())
//          System.out.println(df.format(i)+" - StatusLine="+s);
//      }catch(Exception e){
//        if (config.getDebug())
//          System.out.println(df.format(i)+" - Exception: "+e.getMessage());
//        Thread.sleep(1000l);
//        continue;
//      }finally{
//        if (!config.getDebug())
//          System.out.print(".");
//      }
//      if (!s.contains(config.getExpectedStatusLine())){
////        Thread.sleep(1000);
//        System.out.println("Status wasn't the expected value of ["+config.getExpectedStatusLine()+"]: "+s);
//      }else{
//        Thread.sleep(1000l);
//        System.out.println("Fuse is up!");
//      }
//    }
//  }
}
