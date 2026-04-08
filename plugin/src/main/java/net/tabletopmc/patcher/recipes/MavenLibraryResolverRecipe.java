package net.tabletopmc.patcher.recipes;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

@Value
@EqualsAndHashCode(callSuper = false)
public class MavenLibraryResolverRecipe extends Recipe {
  String displayName = "MavenLibraryResolver util recipe";
  String description = "Inlines the addDependencyCoords/addRepositoryUrl methods.";

  @Override
  public TreeVisitor<?, ExecutionContext> getVisitor() {
    return Visitor.INSTANCE;
  }

  private static final class Visitor extends JavaIsoVisitor<ExecutionContext> {
    private static final String MAVEN_LIBRARY_RESOLVER = "io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver";
    private static final JavaParser.Builder<?, ?> TABLETOP_API_DEPENDENCY = JavaParser.fromJavaVersion()
      .classpath(JavaParser.runtimeClasspath());

    private static final JavaTemplate REPOSITORY_URL_TEMPLATE = JavaTemplate.builder("""
        addRepository(new RemoteRepository.Builder(null, "default", #{any(String)}).build())""")
      .imports("org.eclipse.aether.repository.RemoteRepository")
      .javaParser(TABLETOP_API_DEPENDENCY)
      .build();
    private static final JavaTemplate DEPENDENCY_COORDS_TEMPLATE = JavaTemplate.builder("""
        addDependency(new Dependency(new DefaultArtifact(#{any(String)}), null));""")
      .imports("org.eclipse.aether.graph.Dependency", "org.eclipse.aether.artifact.DefaultArtifact")
      .javaParser(TABLETOP_API_DEPENDENCY)
      .build();

    private static final MethodMatcher ADD_REPOSITORY_URL_MATCHER = new MethodMatcher(MAVEN_LIBRARY_RESOLVER + " addRepositoryUrl(String)");
    private static final MethodMatcher ADD_DEPENDENCY_COORDS_MATCHER = new MethodMatcher(MAVEN_LIBRARY_RESOLVER + " addDependencyCoords(String)");

    public static final Visitor INSTANCE = new Visitor();

    @Override
    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
      if (ADD_REPOSITORY_URL_MATCHER.matches(method)) {
        maybeAddImport("org.eclipse.aether.repository.RemoteRepository");
        return REPOSITORY_URL_TEMPLATE.apply(
          getCursor(),
          method.getCoordinates().replaceMethod(),
          method.getArguments().getFirst()
        );
      }

      if (ADD_DEPENDENCY_COORDS_MATCHER.matches(method)) {
        maybeAddImport("org.eclipse.aether.graph.Dependency");
        maybeAddImport("org.eclipse.aether.artifact.DefaultArtifact");
        return DEPENDENCY_COORDS_TEMPLATE.apply(
          getCursor(),
          method.getCoordinates().replaceMethod(),
          method.getArguments().getFirst()
        );
      }

      return method;
    }
  }
}
