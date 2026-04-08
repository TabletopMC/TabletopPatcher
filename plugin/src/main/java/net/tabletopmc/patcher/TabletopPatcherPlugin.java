package net.tabletopmc.patcher;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;

class TabletopPatcherPlugin implements Plugin<Project> {
  final String PATCHED_SOURCE_SET_NAME = "paperPatched";

  @Override
  public void apply(Project project) {
    final SourceSetContainer sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);

    final Provider<Directory> targetDirectory = project.getLayout().getBuildDirectory().dir("generated/sources/tabletop-patcher/");
    sourceSetContainer.register(PATCHED_SOURCE_SET_NAME, set -> {
      set.getJava().srcDir(targetDirectory);
      set.setCompileClasspath(sourceSetContainer.getByName("main").getCompileClasspath());
    });

    sourceSetContainer.all(sourceSet -> {
      if (sourceSet.getName().equals(PATCHED_SOURCE_SET_NAME)) {
        return;
      }

      project.getTasks().register(SourceSetPatchTask.getTaskName(sourceSet), SourceSetPatchTask.class, task -> {
        task.setSourceSet(sourceSet);
        task.setTargetDirectory(targetDirectory);
      });
    });
  }
}
