plugins {
    id("net.fabricmc.fabric-loom")
}

val minecraftVersion = stonecutter.current.version
val fabricApiVersion = "0.155.2+$minecraftVersion"

group = "me.snowmii"
version = "0.1.0+$minecraftVersion"
base.archivesName = "packhand"

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:0.19.3")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}

loom {
    runs {
        named("client") {
            client()
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraftVersion)
    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to minecraftVersion,
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}
