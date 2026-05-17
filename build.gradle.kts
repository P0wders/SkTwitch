plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
}

group = "com.p0wders"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.skriptlang.org/releases")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.SkriptLang:Skript:2.15.2")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    implementation("com.vdurmont:emoji-java:5.1.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("org.java_websocket", "com.p0wders.sktwitch.libs.websocket")
        relocate("com.vdurmont.emoji", "com.p0wders.sktwitch.libs.emoji")
    }
    build {
        dependsOn(shadowJar)
    }
    processResources {
        val ver = project.version.toString()
        inputs.property("version", ver)
        filesMatching("plugin.yml") {
            expand("version" to ver)
        }
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}