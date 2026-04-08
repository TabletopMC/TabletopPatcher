package net.tabletopmc.test;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;

@SuppressWarnings("UnstableApiUsage")
public class TestPluginLoader implements PluginLoader {
  @Override
  public void classloader(PluginClasspathBuilder builder) {
    final MavenLibraryResolver resolver = new MavenLibraryResolver();
    resolver.addRepositoryUrl(MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR);
    resolver.addDependencyCoords("com.zaxxer:HikariCP:7.0.2");
    resolver.addDependencyCoords("org.glassfish.jaxb:jaxb-runtime:4.0.7");
    resolver.addDependencyCoords("com.h2database:h2:2.4.240");
    builder.addLibrary(resolver);
  }
}
