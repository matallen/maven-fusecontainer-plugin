package org.jboss.fuse.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
          FuseContainer.instance=new FuseContainer();
          FuseContainer.instance.start(this);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Exception: "+e.getMessage(), e);
//        }finally{
//          try{
//            Runtime.getRuntime().exec("stty echo");
//          }catch(Exception sink){}
        }
        System.out.println("EXITING START MOJO!");
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

}
