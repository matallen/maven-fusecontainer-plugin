package org.jboss.fuse.maven;

import static com.jayway.restassured.RestAssured.with;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.IOUtils;
import org.apache.karaf.shell.console.Main;

import com.jayway.restassured.response.Response;

public class FuseContainer2 {
  private static final DecimalFormat df=new DecimalFormat("00");
  private static String PATH=null;
  private static Thread t;
  public void stop(Configuration config) throws InterruptedException{
    System.out.println("YYY STOPPING");
    t.stop();
    System.out.println("YYY STOPPED");
  }
  
  public static void main(String[]args) throws Exception{
    new FuseContainer2().start(new Configuration() {
      public boolean getSkip() {
        return false;
      }
      public String getFuseHome() {
        return "/home/mallen/Work/19578-truphone/jboss-fuse-6.0.0.redhat-024-minimal";
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
//      public String getInstallScript() {
//        return "install.karaf";
//      }
//      public int getInstallScriptTimeout() {
//        return 30;
//      }
      public String[] getCommands(){
        return new String[]{};
      }
      public boolean getClean() {
        return false;
      }
    });
  }
  
  public void start(Configuration config) throws InterruptedException{
    try{
      System.setProperty("karaf.instances", config.getFuseHome()+"/instances");
      System.setProperty("karaf.home",      config.getFuseHome());
      System.setProperty("karaf.base",      config.getFuseHome());
      System.setProperty("karaf.data",      config.getFuseHome()+"/data");
      System.setProperty("java.io.tmpdir",  config.getFuseHome()+"/tmp");
      System.setProperty("java.util.logging.config.file", config.getFuseHome()+"/etc/java.util.logging.properties");
      
//      CLASSPATH="$KARAF_HOME/lib/bin/karaf-client.jar"
//      CLASSPATH="$CLASSPATH:$KARAF_HOME/system/org/apache/sshd/sshd-core/0.8.0/sshd-core-0.8.0.jar"
//      CLASSPATH="$CLASSPATH:$KARAF_HOME/system/org/apache/mina/mina-core/2.0.7/mina-core-2.0.7.jar"
//      CLASSPATH="$CLASSPATH:$KARAF_HOME/system/org/apache/karaf/shell/org.apache.karaf.shell.console/2.3.0.redhat-60024/org.apache.karaf.shell.console-2.3.0.redhat-60024.jar"
      
      
      //exec $JAVA $JAVA_OPTS -Dkaraf.instances="${KARAF_HOME}/instances" -Dkaraf.home="$KARAF_HOME" -Dkaraf.base="$KARAF_BASE" -Djava.io.tmpdir="$KARAF_DATA/tmp" -Djava.util.logging.config.file=$KARAF_BASE/etc/java.util.logging.properties $KARAF_OPTS $OPTS -classpath "$CLASSPATH" org.apache.karaf.client.Main "$@"
      
      System.out.println("Starting: "+config.getFuseHome());
      new Main().run(new String[]{});
      
      PATH=config.getFuseHome();
      t=startContainer(config.getFuseHome()+"/karaf");
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
      String[] commandArray=new String[]{PATH+"/client","-u","admin","-p","admin", "$COMMAND"};
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
