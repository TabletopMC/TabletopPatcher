plugins {
  id("java-library")
  id("net.tabletopmc.tabletop-patcher")
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  compileOnly(libs.tabletop.api)
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
  withType<JavaCompile>().configureEach {
    options.release = 25;
  }
}
