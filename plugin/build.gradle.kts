plugins {
  id("java-gradle-plugin")
  id("maven-publish")
  alias(libs.plugins.recipe.library)
  alias(libs.plugins.shadow)
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  implementation(platform(libs.openrewrite.bom))
  implementation(libs.openrewrite.java)
  runtimeOnly(libs.openrewrite.java.jdk25)
  implementation(libs.gson)

  compileOnly(libs.lombok)
  annotationProcessor(libs.lombok)

  testImplementation(libs.openrewrite.test)
  testImplementation(libs.jupiter.api)
  testImplementation(libs.jupiter.params)
  testRuntimeOnly(libs.jupiter.engine)
  testImplementation(libs.tabletop.api)
}

recipeDependencies {
  parserClasspath("net.tabletopmc.tabletop:tabletop-api:26.1.1-SNAPSHOT")
  parserClasspath("org.apache.maven.resolver:maven-resolver-api:1.9.25")
}

gradlePlugin {
  vcsUrl = "https://github.com/TabletopMC/TabletopPatcher"
  plugins.register("tabletop-patcher") {
    id = "net.tabletopmc.tabletop-patcher"
    implementationClass = "net.tabletopmc.patcher.TabletopPatcherPlugin"
    description = "Gradle plugin for crosscompiling from Tabletop API to Paper API."
  }
}

publishing {
  repositories {
    maven("https://eldonexus.com/maven-releases/") {
      name = "EldoNexus"
      credentials {
        username = System.getenv("MAVEN_USERNAME") ?: ""
        password = System.getenv("MAVEN_PASSWORD") ?: ""
      }
    }
  }
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
  processResources {
    dependsOn(createTypeTable)
  }
  withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.release = 25;
  }
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter(libs.versions.junit)
    }
  }
}
