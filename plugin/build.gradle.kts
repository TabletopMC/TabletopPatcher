plugins {
  id("java-gradle-plugin")
  id("maven-publish")
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

gradlePlugin {
  val patcher by plugins.creating {
    id = "net.tabletopmc.tabletop-patcher"
    implementationClass = "net.tabletopmc.patcher.TabletopPatcherPlugin"
  }
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
  options.compilerArgs.add("-parameters")
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter(libs.versions.junit)
    }
  }
}
