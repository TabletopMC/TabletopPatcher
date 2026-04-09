package net.tabletopmc.patcher;

import net.tabletopmc.patcher.recipes.MavenLibraryResolverRecipe;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.jspecify.annotations.NullMarked;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.LargeSourceSet;
import org.openrewrite.Parser;
import org.openrewrite.Recipe;
import org.openrewrite.RecipeRun;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.JavaParser;
import org.openrewrite.tree.ParsingExecutionContextView;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@NullMarked
class SourceSetPatchTask extends DefaultTask {
  private static final Logger LOGGER = Logging.getLogger(SourceSetPatchTask.class);
  private static final List<Recipe> RECIPES = List.of(
    new MavenLibraryResolverRecipe()
  );

  private final SetProperty<File> sourceDirectories;
  private final DirectoryProperty targetDirectory;

  @Inject
  public SourceSetPatchTask(ObjectFactory objects) {
    sourceDirectories = objects.setProperty(File.class);
    targetDirectory = objects.directoryProperty();
  }

  void setTargetDirectory(Provider<Directory> targetDirectory) {
    this.targetDirectory.value(targetDirectory);
  }

  void setSourceSet(SourceSet sourceSet) {
    this.sourceDirectories.value(sourceSet.getJava().getSrcDirs());
  }

  @TaskAction
  void patchIntoGeneratedSourceSet() {
    final ExecutionContext ctx = ParsingExecutionContextView.view(new InMemoryExecutionContext(Throwable::printStackTrace));
    final JavaParser parser = JavaParser.fromJavaVersion()
      .classpathFromResources(ctx, "tabletop-api", "maven-resolver-api")
      .build();

    for (File rootFile : sourceDirectories.get()) {
      final Path rootPath = rootFile.toPath();
      try (Stream<Path> paths = Files.walk(rootPath)) {
        final Stream<Parser.Input> inputs = paths
          .filter(Files::isRegularFile)
          .map(path -> {
            try {
              return Parser.Input.fromString(path, Files.readString(path));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

        final List<SourceFile> sourceFiles = parser.parseInputs(inputs.toList(), rootPath, ctx).toList();
        final List<SourceFile> modifiedFiles = applyRecipes(ctx, sourceFiles);

        final Set<Path> copiedPaths = new HashSet<>();
        for (SourceFile modifiedFile : modifiedFiles) {
          final String content = modifiedFile.printAll();
          final RegularFile targetFile = targetDirectory.get().file(modifiedFile.getSourcePath().toString());
          final Path targetPath = targetFile.getAsFile().toPath();
          LOGGER.lifecycle("Copying modified file: " + targetPath);

          if (!Files.deleteIfExists(targetPath)) {
            Files.createDirectories(targetPath.getParent());
          }

          Files.createFile(targetPath);
          Files.writeString(targetPath, content);
          copiedPaths.add(targetPath);
        }

        for (SourceFile sourceFile : sourceFiles) {
          final RegularFile targetFile = targetDirectory.get().file(sourceFile.getSourcePath().toString());
          final Path targetPath = targetFile.getAsFile().toPath();
          if (copiedPaths.contains(targetPath)) {
            continue;
          }

          LOGGER.lifecycle("Copying source file: " + targetPath);
          if (!Files.deleteIfExists(targetPath)) {
            Files.createDirectories(targetPath.getParent());
          }

          Files.createFile(targetPath);
          Files.writeString(targetPath, sourceFile.printAll());
          copiedPaths.add(targetPath);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private List<SourceFile> applyRecipes(ExecutionContext ctx, List<SourceFile> sourceFiles) {
    final List<SourceFile> before = new ArrayList<>(sourceFiles);
    final List<SourceFile> after = new ArrayList<>(sourceFiles.size());

    for (final Recipe recipe : RECIPES) {
      after.clear();

      final LargeSourceSet lss = new InMemoryLargeSourceSet(before);
      final RecipeRun run = recipe.run(lss, ctx);

      after.addAll(run.getChangeset().getAllResults().stream()
        .map(Result::getAfter)
        .filter(Objects::nonNull)
        .toList()
      );
      before.clear();
      before.addAll(after);
    }

    return after;
  }

  private static String getSourceSetNameCapitalized(SourceSet sourceSet) {
    return Character.toUpperCase(sourceSet.getName().charAt(0)) + sourceSet.getName().substring(1);
  }

  public static String getTaskName(SourceSet set) {
    return "patch" + getSourceSetNameCapitalized(set) + "ForPaper";
  }
}
