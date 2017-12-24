package se.bjurr.violations.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ViolationsGradlePlugin implements Plugin<Project> {
  @Override
  public void apply(Project target) {
    target.getExtensions().create("violationsPlugin", ViolationsPluginExtension.class);
  }
}
