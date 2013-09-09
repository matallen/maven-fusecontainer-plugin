package org.jboss.fuse.maven;

public class Container {
  public String groupId;
  public String artifactId;
  public String version;
  public String type="zip";

  public String toString() {
    return "Container[groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", type=" + type + "]";
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean populated() {
    return groupId!=null && artifactId!=null && version!=null;
  }

}
