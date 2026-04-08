plugins {
  id("java-gradle-plugin")
  id("maven-publish")
  alias(libs.plugins.recipe.library)
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  implementation(platform(libs.openrewrite.bom))
  implementation(libs.openrewrite.java)
  runtimeOnly(libs.openrewrite.java.jdk25)

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
  parserClasspath("org.apache.maven:maven-resolver-provider:3.9.6")
}

gradlePlugin {
  val patcher by plugins.creating {
    id = "net.tabletopmc.tabletop-patcher"
    implementationClass = "net.tabletopmc.patcher.TabletopPatcherPlugin"
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
