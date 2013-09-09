package org.jboss.fuse.maven;

public interface Configuration {
//  public String getType();
//  
//  public String getPingUrl();
  public String getFuseHome();
  public boolean getSkip();
  public boolean getDebug();
  public int getTimeout();
  public String getClientUsername();
  public String getClientPassword();
//  public String getInstallScript();
//  public int getInstallScriptTimeout();
  public String[] getCommands();
  
  public boolean getClean();
//  public String getExpectedStatusLine();
//  public Integer getSecondsToWait();
//  
//  public String getScript();
//  public String getScriptResultContains();
}
