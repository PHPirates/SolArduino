// Gradle file from https://github.com/PHPirates/kotlin-template-project

import org.gradle.api.plugins.ExtensionAware

import org.gradle.jvm.tasks.Jar

group = "solarduino"
version = "v1.2.1"

plugins {
    application
    java // Required by at least JUnit.

    // Plugin to build .exe files.
    id("edu.sc.seis.launch4j") version "2.4.4"

    // help/dependencyUpdates checks for dependency updates.
    id("com.github.ben-manes.versions") version "0.19.0"

    // help/useLatestVersions should update version numbers
    id("se.patrikerdes.use-latest-versions") version "0.2.3"
}

launch4j {
    mainClassName = "nl.deltadak.solarduino.Main"
    icon = "$projectDir/release/icon/solarduino-desktop.ico"
    manifest = "$projectDir/release/launch4j/solarduino.manifest"
}

application {
    mainClassName = "nl.deltadak.solarduino.Main"
}

dependencies {
    // JNA, used to e.g. make a program pinnable to taskbar.
    compile("net.java.dev.jna:jna:4.5.1")
    compile("net.java.dev.jna:jna-platform:4.5.1")
}

repositories {
    mavenCentral()
    jcenter()
    mavenLocal()
}

/** Build an executable jar. */
val fatJar = task("fatJar", type = Jar::class) {
    baseName = project.name
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "nl.deltadak.solarduino.Main"
    }
    from(configurations.runtime.map({
        @Suppress("IMPLICIT_CAST_TO_ANY")
        if (it.isDirectory) it else zipTree(it)
    }))
    with(tasks["jar"] as CopySpec)
}
