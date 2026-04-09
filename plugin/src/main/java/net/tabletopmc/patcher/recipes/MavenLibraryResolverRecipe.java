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
    private static final MethodMatcher ADD_REPOSITORY_URL_MATCHER = new MethodMatcher(MAVEN_LIBRARY_RESOLVER + " addRepositoryUrl(String)");
    private static final MethodMatcher ADD_DEPENDENCY_COORDS_MATCHER = new MethodMatcher(MAVEN_LIBRARY_RESOLVER + " addDependencyCoords(String)");

    public static final Visitor INSTANCE = new Visitor();

    @Override
    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
      if (ADD_REPOSITORY_URL_MATCHER.matches(method)) {
        final JavaTemplate repositoryUrlTemplate = JavaTemplate.builder("""
            #{any(%s)}.addRepository(new RemoteRepository.Builder(null, "default", #{any(String)}).build());"""
            .formatted(MAVEN_LIBRARY_RESOLVER)
            .trim())
          .imports("org.eclipse.aether.repository.RemoteRepository", "org.eclipse.aether.repository.RemoteRepository.Builder")
          .contextSensitive()
          .javaParser(constructJavaParser(ctx))
          .build();

        maybeAddImport("org.eclipse.aether.repository.RemoteRepository");
        return super.visitMethodInvocation(repositoryUrlTemplate.apply(
          getCursor(),
          method.getCoordinates().replaceMethod(),
          method.getSelect(), method.getArguments().getFirst()
        ), ctx);
      }

      if (ADD_DEPENDENCY_COORDS_MATCHER.matches(method)) {
        final JavaTemplate dependencyCoordsTemplate = JavaTemplate.builder(
            "#{any(%s)}.addDependency(new Dependency(new DefaultArtifact(#{any(String)}), null));"
              .formatted(MAVEN_LIBRARY_RESOLVER))
          .imports("org.eclipse.aether.graph.Dependency", "org.eclipse.aether.artifact.DefaultArtifact")
          .contextSensitive()
          .javaParser(constructJavaParser(ctx))
          .build();

        maybeAddImport("org.eclipse.aether.graph.Dependency");
        maybeAddImport("org.eclipse.aether.artifact.DefaultArtifact");
        return super.visitMethodInvocation(dependencyCoordsTemplate.apply(
          getCursor(),
          method.getCoordinates().replaceMethod(),
          method.getSelect(), method.getArguments().getFirst()
        ), ctx);
      }

      return super.visitMethodInvocation(method, ctx);
    }

    private JavaParser.Builder<?, ?> constructJavaParser(ExecutionContext ctx) {
      return JavaParser.fromJavaVersion()
        .classpathFromResources(ctx, "tabletop-api", "maven-resolver-api");
    }
  }
}
