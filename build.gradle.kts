@file:Suppress("UnstableApiUsage")

plugins {
    java
    idea
    id("quiet-fabric-loom") version ("1.7-SNAPSHOT")
    id("org.jetbrains.kotlin.jvm").version("2.0.0")
}

val modId = project.properties["mod_id"].toString()
version = project.properties["version"].toString()
group = project.properties["group"].toString()

val minecraftVersion = project.properties["minecraft_version"].toString()

base.archivesBaseName = project.properties["mod_name"].toString()

repositories {
    mavenCentral()
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.parchmentmc.org")
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://repo.lucko.me")
}

loom {
    splitEnvironmentSourceSets()
    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val modImplementationInclude by configurations.register("modImplementationInclude")

configurations {
    modImplementationInclude
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        // TODO: Fix hardcoded minecraft version once Parchment updates
        parchment("org.parchmentmc.data:parchment-1.21:${project.properties["parchment_version"]}")
    })
    modImplementation("net.fabricmc:fabric-loader:${project.properties["loader_version"].toString()}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"].toString()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"].toString()}")

    // Impactor
    modImplementation("net.impactdev.impactor:common:5.3.0+1.21.1-SNAPSHOT")
    modImplementation("net.impactdev.impactor.api:economy:5.3.0-SNAPSHOT")
    modImplementation("net.impactdev.impactor.api:text:5.3.0-SNAPSHOT")

    modCompileOnly("ca.landonjw.gooeylibs:fabric-api-repack:3.1.0-1.21.1-SNAPSHOT@jar")?.let {
        include(it)
    }

    modImplementation("com.cobblemon:fabric:1.6.0+1.21.1-SNAPSHOT")

    // Placeholders
    modImplementation("eu.pb4:placeholder-api:2.4.1+1.21")
    modImplementation("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    modImplementation("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")

    // Adventure Text!
    modImplementation(include("net.kyori:adventure-platform-fabric:5.14.2")!!)

    // PermissionsAPI
    modImplementation("me.lucko:fabric-permissions-api:0.3.1")?.let {
        include(it)
    }

    // Mongo Database
    implementation(include("org.mongodb:mongodb-driver-sync:4.11.0")!!)
    implementation(include("org.mongodb:mongodb-driver-core:4.11.0")!!)
    implementation(include("org.mongodb:bson:4.11.0")!!)

    // SQL Storage
    implementation(include("org.mariadb.jdbc:mariadb-java-client:3.1.0")!!)
    implementation(include("com.zaxxer:HikariCP:5.1.0")!!)
    implementation(include("org.xerial:sqlite-jdbc:3.43.2.2")!!)
    implementation(include("com.h2database:h2:2.2.224")!!)
    implementation(include("com.mysql:mysql-connector-j:8.2.0")!!)

    modCompileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("id" to modId, "version" to version)
    }

    filesMatching("**/lang/*.json") {
        expand("id" to modId)
    }
}

tasks.remapJar {
    archiveFileName.set("${project.name}-fabric-$minecraftVersion-${project.version}.jar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask> {
    from("LICENSE") {
        rename { "${it}_${modId}" }
    }
}
