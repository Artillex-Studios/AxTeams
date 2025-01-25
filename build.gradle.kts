plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.2")
}

group = "com.artillexstudios.axteams"
version = "1.0.0"

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))
}

allprojects {
    repositories {
        mavenCentral()

        maven("https://jitpack.io/")
        maven("https://repo.artillex-studios.com/releases/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("com.gradleup.shadow")
    }

    dependencies {
        implementation("com.artillexstudios.axapibootstrapper:axapi-bootstrapper:1.0.21:all")
        compileOnly("com.artillexstudios.axapi:axapi:1.4.531:all")
        compileOnly("dev.jorel:commandapi-bukkit-shade:9.7.0")
        compileOnly("com.h2database:h2:2.3.232")
        compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
        compileOnly("org.apache.commons:commons-lang3:3.14.0")
        compileOnly("dev.triumphteam:triumph-gui:3.1.10")
        compileOnly("com.github.MilkBowl:VaultAPI:1.7")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("commons-io:commons-io:2.16.1")
        compileOnly("it.unimi.dsi:fastutil:8.5.13")
        compileOnly("org.slf4j:slf4j-api:2.0.9")
        compileOnly("com.zaxxer:HikariCP:5.1.0")
        compileOnly("org.jooq:jooq:3.19.10")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
//        relocate("com.github.benmanes", "com.artillexstudios.axteams.libs.axapi.libs.caffeine")
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axteams.libs.axapi")
        relocate("dev.jorel.commandapi", "com.artillexstudios.axteams.libs.commandapi")
        relocate("dev.triumphteam.gui", "com.artillexstudios.axteams.libs.triumphgui")
        relocate("com.zaxxer", "com.artillexstudios.axteams.libs.hikaricp")
        relocate("org.jooq", "com.artillexstudios.axteams.libs.jooq")
        relocate("org.h2", "com.artillexstudios.axteams.libs.h2")
    }
}