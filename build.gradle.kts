plugins {
    java
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.60"
    id("com.github.johnrengelman.shadow") version "4.0.4"

    id("com.qixalite.spongestart2") version "4.0.0"
}

group = "arvenwood"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.spongepowered.org/maven")

    // GriefDefender
    maven("https://jitpack.io")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

    // Shaded
    runtime(kotlin("stdlib-jdk8"))
    runtime(kotlin("reflect"))
    runtime("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    runtime("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

    // Sponge
    compileOnly("org.spongepowered:spongeapi:7.1.0")
    kapt("org.spongepowered:spongeapi:7.1.0")

    // GriefDefender
    compileOnly("com.github.bloodmc:GriefDefenderAPI:b956577866")

    // PlaceholderAPI
    compileOnly("com.github.ronaldburns:PlaceholderAPI:4.5.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val shadowJar: com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar by tasks

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes")
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    shadowJar {
        archiveClassifier.set("dist")
    }
}

spongestart {
    minecraft = "1.12.2"
}

artifacts {
    add("archives", shadowJar)
}