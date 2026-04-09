# Tabletop Patcher

This Gradle plugin allows projects utilizing the [TabletopPaper](https://github.com/TabletopMC/TabletopPaper)
API to cross-build Paper-compatible JAR files. Enjoy the benefits of TabletopPaper API, whilst keeping
compatible with all Paper-based server software.

## Installation

In order to use this Gradle plugin, you will need Gradle. The latest version is recommended. Furthermore, you
need Java 25.

Currently, tabletop-patcher is published on the [Eldonexus](https://eldonexus.de). You will need to add the
following block to your `settings.gradle.kts`:

```kts
pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://eldonexus.de/repository/maven-public/")
  }
}
```

Now, you can apply the plugin inside your `build.gradle.kts`:

```kts
plugins {
  // If needed, update the version. Newer versions provide more
  // features, fixes, and rewrite mappings.
  id("net.tabletopmc.tabletop-patcher") version "1.0.0"
}
```

## Tasks

Once the plugin is installed, you will have access to one new task:

- `:buildForPaper`

This task uses your regular `build` configuration and rewrites the code automatically
in order to produce a Paper-compatible JAR file. You can find the built JAR inside you
`build/libs` directory. It will have a `-paper` prefix.

## How does it work?

Internally, the plugin utilizes [OpenRewrite](https://github.com/openrewrite) to rewrite your source files,
copying them into a new source set in the process (`build/tabletop-patcher/patched`). You can view this
source set like you would any other source set, allowing you to control the rewritten code.

Currently, only source files inside the `main` source set are rewritten. Depending on demand, this
restriction may be lifted in the future.
