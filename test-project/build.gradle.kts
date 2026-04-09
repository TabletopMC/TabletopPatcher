plugins {
  id("java-library")
  id("net.tabletopmc.tabletop-patcher")
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://eldonexus.de/repository/maven-public/")
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
