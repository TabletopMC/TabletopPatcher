package net.tabletopmc.patcher.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MavenLibraryResolverRecipeTest implements RewriteTest {
  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(new MavenLibraryResolverRecipe());
  }

  @Test
  void testRewrite() {
    rewriteRun(
      java(
        """
          package net.tabletopmc.testplugin;
          
          import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
          import io.papermc.paper.plugin.loader.PluginLoader;
          import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
          
          class TestPluginLoader implements PluginLoader {
            @Override
            public void classloader(PluginClasspathBuilder builder) {
              final MavenLibraryResolver maven = new MavenLibraryResolver();
              maven.addRepositoryUrl("https://maven.miles.sh/snapshots");
              maven.addDependencyCoords("sh.miles.menukit:menukit-core:1.0.0-SNAPSHOT");
              maven.addDependencyCoords("sh.miles.menukit:menukit-strings:1.3.0-SNAPSHOT");
              builder.addLibrary(maven);
            }
          }""",
        """
          package net.tabletopmc.testplugin;
          
          import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
          import io.papermc.paper.plugin.loader.PluginLoader;
          import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
          import org.eclipse.aether.artifact.DefaultArtifact;
          import org.eclipse.aether.graph.Dependency;
          import org.eclipse.aether.repository.RemoteRepository;
          
          class TestPluginLoader implements PluginLoader {
            @Override
            public void classloader(PluginClasspathBuilder builder) {
              final MavenLibraryResolver maven = new MavenLibraryResolver();
              maven.addRepository(new RemoteRepository.Builder(null, "default", "https://maven.miles.sh/snapshots").build());
              maven.addDependency(new Dependency(new DefaultArtifact("sh.miles.menukit:menukit-core:1.0.0-SNAPSHOT"), null));
              maven.addDependency(new Dependency(new DefaultArtifact("sh.miles.menukit:menukit-strings:1.3.0-SNAPSHOT"), null));
              builder.addLibrary(maven);
            }
          }"""
      )
    );
  }
}
