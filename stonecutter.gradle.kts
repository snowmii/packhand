plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.17.16" apply false
}

stonecutter active "26.2" /* [SC] DO NOT EDIT */

subprojects {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}
