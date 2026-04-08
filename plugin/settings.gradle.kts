import org.gradle.kotlin.dsl.support.uppercaseFirstChar

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "tabletop-patcher"
rootProject.name.uppercaseFirstChar()
