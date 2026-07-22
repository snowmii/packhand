plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.17.16" apply false
}

stonecutter active "26.2" /* [SC] DO NOT EDIT */

stonecutter.tasks.order("build")

tasks.register("buildAll") {
    group = "project"
    description = "Builds the mod for every configured Minecraft version"
    dependsOn(stonecutter.tasks.named("build").map { it.values })
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}
