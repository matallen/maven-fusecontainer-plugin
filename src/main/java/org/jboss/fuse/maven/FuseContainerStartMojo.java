package org.jboss.fuse.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

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
import org.apache.maven.project.MavenProject;

/**
 * @goal start
 * @phase pre-integration-test
 * @requiresDependencyResolution compile
 * @threadSafe
 * @see <a href="http://maven.apache.org/developers/mojo-api-specification.html">Mojo API Specification</a>
 */
public class FuseContainerStartMojo extends AbstractFuseContainerMojo implements Configuration {
  
    public void execute() throws MojoExecutionException, MojoFailureException{
        if (getSkip())
            return;
        
        addProjectDependenciesToClasspath();
        try {
          // download fuse and/or set fuse home
          configureContainer();
          
          // check users.properties
          configureUsersProperties();
          
          // TODO: copy clean fuse instance (if configured from fuseHome property) to target/container folder
          FuseContainer.instance=new FuseContainer();
          FuseContainer.instance.start(this);
        } catch (Exception e) {
            throw new MojoExecutionException("Exception: "+e.getMessage(), e);
//        }finally{
//          try{
//            Runtime.getRuntime().exec("stty echo");
//          }catch(Exception sink){}
        }
        if (getDebug()) System.out.println("Fuse Started!");
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
        }
        throw new RuntimeException("What is this?");
    }
    
    public void configureUsersProperties() throws FileNotFoundException, IOException{
      File propertiesFile=new File(getFuseHome(), "etc/users.properties");
//      System.out.println("FILE="+propertiesFile.getPath());
      String userPropertiesContents=IOUtils.toString(new FileInputStream(propertiesFile));
//      System.out.println("contents="+userPropertiesContents);
      
      Matcher matcher = Pattern.compile(".+#admin=.+", Pattern.DOTALL).matcher(userPropertiesContents);
//      System.out.println("matcher.matches = "+matcher.matches());
      if (matcher.matches()){
        userPropertiesContents+="\nadmin=admin,admin";
        IOUtils.write(userPropertiesContents, new FileOutputStream(propertiesFile));
        if (getDebug()) System.out.println("automatically appended user information to interact with container");
      }else{
        if (getDebug()) System.out.println("not appending user information");
      }
    }

}
