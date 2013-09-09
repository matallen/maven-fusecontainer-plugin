package org.jboss.fuse.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

public abstract class AbstractFuseContainerMojo extends AbstractMojo implements Configuration {

    /**
     * @parameter expression="${fusecontainer.skip}" default-value="false"
     * @since 1.0.0
     */
    private boolean skip = false;
    public boolean getSkip() { return skip; }
    
    /**
     * @parameter expression="${fusecontainer.debug}" default-value="false"
     * @since 1.0.0
     */
    private boolean debug = false;
    public boolean getDebug() { return debug; }

    /**
     * @parameter expression="${fusecontainer.fuseHome}" default-value="false"
     * @since 1.0.0
     */
    private String fuseHome = "";
    public String getFuseHome() { return fuseHome; }
    public void setFuseHome(String fuseHome){ this.fuseHome=fuseHome; }
    
    /**
     * @parameter expression="${fusecontainer.timeout}" default-value="30"
     * @since 1.0.0
     */
    private int timeout = 30;
    public int getTimeout() { return timeout; }
    
    /**
     * @parameter expression="${fusecontainer.clientUsername}" default-value="admin"
     * @since 1.0.0
     */
    private String clientUsername = "admin";
    public String getClientUsername() { return clientUsername; }
    
    /**
     * @parameter expression="${fusecontainer.clientPassword}" default-value="admin"
     * @since 1.0.0
     */
    private String clientPassword = "admin";
    public String getClientPassword() { return clientPassword; }
    
//    /**
//     * @parameter expression="${fusecontainer.installScript}"
//     * @since 1.0.0
//     */
//    private String installScript = null;
//    public String getInstallScript() { return installScript; }
    
//    /**
//     * @parameter expression="${fusecontainer.installScriptTimeout}" default-value="30"
//     * @since 1.0.0
//     */
//    private int installScriptTimeout = 30;
//    public int getInstallScriptTimeout() { return installScriptTimeout; }
    
    /**
    * @parameter expression="${fusecontainer.commands}"
    * @since 1.0.0
    */
    private String[] commands;
    public String[] getCommands() { return commands; }
 
    /** @parameter expression="${fusecontainer.container}" */
    protected Container container;
    
    /**
     * @parameter expression="${fusecontainer.clean}" default-value="false"
     * @since 1.0.0
     */
    private boolean clean = false;
    public boolean getClean() {                 return clean; }
    
    /**
     * The project being built.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    public MavenProject getProject(){return project;}
}
