plugins {
    `java-library`
}

group = "com.zach"
version = "2.0.3"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.13-R0.1-SNAPSHOT")
    compileOnly(files("src/libs/worldguard-bukkit-7.0.7-dist.jar"))
    compileOnly(files("src/libs/worldedit-bukkit-7.2.10.jar"))
    compileOnly(files("src/libs/ActionAnnouncer-1.16.4.jar"))
    compileOnly(files("src/libs/Essentials.jar"))
    compileOnly("org.junit.jupiter:junit-jupiter:5.8.2")
}

val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible)
            options.release.set(targetJavaVersion)
    }
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
    test {
        useJUnitPlatform()
    }
}
