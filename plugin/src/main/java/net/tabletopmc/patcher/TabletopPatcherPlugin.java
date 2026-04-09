package net.tabletopmc.patcher;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;

class TabletopPatcherPlugin implements Plugin<Project> {
  private static final String PATCHED_SOURCE_SET_NAME = "paperPatched";

  @Override
  public void apply(Project project) {
    if (!project.getPlugins().hasPlugin("java") && !project.getPlugins().hasPlugin("java-library")) {
      return;
    }

    final SourceSetContainer sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
    final SourceSet mainSourceSet = sourceSetContainer.getByName("main");

    final DirectoryProperty buildDirectory = project.getLayout().getBuildDirectory();
    final Provider<Directory> targetDirectory = buildDirectory.dir("tabletop-patcher/patched");
    sourceSetContainer.register(PATCHED_SOURCE_SET_NAME, set -> {
      set.getJava().srcDir(targetDirectory);
      set.setCompileClasspath(mainSourceSet.getCompileClasspath());
      set.setRuntimeClasspath(mainSourceSet.getRuntimeClasspath());
    });

    project.getTasks().register(SourceSetPatchTask.getTaskName(mainSourceSet), SourceSetPatchTask.class, task -> {
      task.setDescription("Patches the main source with Paper-compatible code.");

      task.setSourceSet(mainSourceSet);
      task.setProjectDir(buildDirectory);
      task.setTargetDirectory(targetDirectory);
    });

    project.getTasks().register("buildForPaper", Jar.class, (task) -> {
      task.setGroup("build");
      task.setDescription("Creates a Paper-compatible JAR file.");

      task.dependsOn(SourceSetPatchTask.getTaskName(mainSourceSet));
      task.from(sourceSetContainer.getByName(PATCHED_SOURCE_SET_NAME).getOutput());
      task.getArchiveClassifier().set("paper");
    });
  }
}
