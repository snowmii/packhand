pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    create(rootProject) {
        vcsVersion = "26.2"
        versions("26.1.2", "26.2")
    }
}

rootProject.name = "packhand"
