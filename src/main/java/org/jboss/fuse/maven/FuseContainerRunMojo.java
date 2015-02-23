package org.jboss.fuse.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal run
 * @phase pre-integration-test
 * @requiresDependencyResolution compile
 * @threadSafe
 * @see <a href="http://maven.apache.org/developers/mojo-api-specification.html">Mojo API Specification</a>
 */
public class FuseContainerRunMojo extends AbstractFuseContainerMojo implements Configuration {
  
    public void execute() throws MojoExecutionException, MojoFailureException{
        if (getSkip())
            return;
        
        addProjectDependenciesToClasspath();
        try {
          // download fuse and/or set fuse home
          configureContainer();
          
          // check users.properties
          configureUsersProperties();
          
          configureOtherEtcProperties();
          
          // TODO: copy clean fuse instance (if configured from fuseHome property) to target/container folder
          FuseContainer.instance=new FuseContainer();
          FuseContainer.instance.start(this);
          
          System.out.println("");
          getLog().info("Press Ctrl-C to stop the container...");
//          System.out.println("Press Ctrl-C to stop the container...");
//          BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
//          while(true){
//            String msg;
//            try{
//              msg=br.readLine();
//            }catch(Exception e){}
//            if(msg.equals("Q")) {quit=1;break;}
//          }
          
          if (getDebug()) System.out.println("Fuse Started!");
          
//          Runtime.getRuntime().addShutdownHook(new Thread(
//              new Runnable() {
//                @Override
//                public void run() {
//                  System.out.println("inside ShutdownHook");
//                  FuseContainer.instance.setExit(true);
//                  System.exit(0);
//                }
//              }
//              ));
          
          System.out.println("");
          while (true){
            int i=System.in.read();
            System.out.print(i);
            if (i==3){ // ctrl-c
              FuseContainer.instance.setExit(true);
              break;
            }
//            System.out.print(".");
//            Thread.currentThread().sleep(10000l);
          }
          
//          
//          
//          
//          while(true){
//            br.read()
//          }
          
          
        } catch (Exception e) {
            throw new MojoExecutionException("Exception: "+e.getMessage(), e);
        }
//        if (getDebug()) System.out.println("Fuse Started!");
    }

    private void addProjectDependenciesToClasspath() {
        try {
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newClassLoader = new ProjectClasspath().getClassLoader(getProject(), oldClassLoader, getLog());
            Thread.currentThread().setContextClassLoader(newClassLoader);
        } catch (DependencyResolutionRequiredException e) {
            getLog().info("Skipping addition of project artifacts, there appears to be a dependecy resolution problem",e);
        }
    }
    
    
    /**
     * Used to look up Artifacts in the remote repository.
     * @parameter expression=
     *  "${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    protected ArtifactFactory factory;
    /**
     * Used to look up Artifacts in the remote repository.
     * @parameter expression=
     *  "${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    protected ArtifactResolver artifactResolver;
    /**
     * List of Remote Repositories used by the resolver
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List remoteRepositories;
    /**
     * Location of the local repository.
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;
    
    
    public void configureContainer(){
      try{
        if (container.populated()){
          File location=download(
              container.getGroupId(), 
              container.getArtifactId(),
              container.getVersion(),
              container.getType());
          File container=new File(getProject().getBuild().getDirectory(), "container");
          new UnZip().extract(location, container);
          
          File[] containerSubfolders=container.listFiles();
          if (containerSubfolders.length!=1)
            throw new RuntimeException("Unable to find fuse home, artifact contained more than 1 subfolder");
          
          setFuseHome(containerSubfolders[0].getCanonicalPath());
        }else if (getFuseHome()!=null){
          setFuseHome(new File(getFuseHome()).getCanonicalPath());
        }else{
          throw new RuntimeException("You must set either the <fuseHome> or <container> element.");
        }
      }catch(IOException e){
        throw new RuntimeException("Unable to determine fuseHome canonical location", e);
      }
    }
    
    public File download(String groupId, String artifactId, String version, String type) {
        try {
          Artifact pomArtifact = this.factory.createArtifact(groupId, artifactId, version, "", type);
          artifactResolver.resolve(pomArtifact, this.remoteRepositories, this.localRepository);
          
          if (getDebug()) System.out.println("Resolved Artifact GAV to ["+pomArtifact.getFile()+"]");
          return pomArtifact.getFile();
          
        } catch (ArtifactResolutionException e) {
          getLog().error("can't resolve parent pom", e);
        } catch (ArtifactNotFoundException e) {
          getLog().error("can't resolve parent pom", e);
        } catch (Exception e){
          getLog().error(e.getMessage(), e);
        }
        throw new RuntimeException("What is this?");
    }
    
    public void configureUsersProperties() throws FileNotFoundException, IOException{
      File propertiesFile=new File(getFuseHome(), "etc/users.properties");
      String userPropertiesContents=IOUtils.toString(new FileInputStream(propertiesFile));
      Matcher matcher = Pattern.compile(".+#admin=.+", Pattern.DOTALL).matcher(userPropertiesContents);
      if (matcher.matches()){
        userPropertiesContents+="\nadmin=admin,admin";
        IOUtils.write(userPropertiesContents, new FileOutputStream(propertiesFile));
        if (getDebug()) System.out.println("automatically appended user information to interact with container");
      }else{
        if (getDebug()) System.out.println("not appending user information");
      }
    }
    
    public void configureOtherEtcProperties(){
      if (getDebug()) System.out.println(getProperties().size()+" property updates found");
      for (Map.Entry<String, String> e:getProperties().entrySet()){
        File file=new File(getFuseHome(), "etc/"+e.getKey().split("_")[0]+".cfg");
        if (!file.exists()) throw new RuntimeException("Unable to find file ["+file.getPath()+"] to apply property update");
        
        String property=e.getKey().split("_")[1];
        boolean append=false;
        if (e.getKey().split("_").length>2)
          append=e.getKey().split("_")[2].equalsIgnoreCase("append");
//          append=Boolean.parseBoolean(e.getKey().split("_")[2]);
        String newValue=e.getValue();
        if (getDebug()) System.out.println("Property update: file=["+file+"], property=["+property+"], value=["+newValue+"], append=["+append+"]");
        
        // make the property change
        try {
          if (!file.getCanonicalFile().exists()) throw new RuntimeException("configuration file "+file.getCanonicalPath()+" cannot be found");
          org.apache.felix.utils.properties.Properties p=new org.apache.felix.utils.properties.Properties(file);
          p.save(new File(file.getParentFile(), file.getName()+".bak"));
          String value=append?p.get(property)+newValue:newValue;
          if (getDebug()) System.out.println("changing value from ["+p.get(property) +"] to ["+value+"]");
          p.setProperty(property, value);
          p.save(file);
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }

}
