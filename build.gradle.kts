plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://maven.pvphub.me/tofaa") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.extendedclip.com/releases/") }
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
    compileOnly(files("../Jarona/build/libs/Jarona-" + providers.gradleProperty("jarona_version").get() + "-all.jar"))
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.7")
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("com.github.Lodestones:Sign-API:1.0.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
        pluginJars(files("../Jarona/build/libs/Jarona-" + providers.gradleProperty("jarona_version").get() + "-all.jar"))
        downloadPlugins {
            modrinth("packetevents", "2.13.0+spigot")
            modrinth("nbtapi", "2.15.7")
        }
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
