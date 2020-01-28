plugins {
    java
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.60"
    id("com.github.johnrengelman.shadow") version "4.0.4"
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

    // Kotlin shading
    runtime(kotlin("stdlib-jdk8"))
    runtime("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    runtime("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

    // Sponge
    compileOnly("org.spongepowered:spongeapi:7.1.0")
    kapt("org.spongepowered:spongeapi:7.1.0")

    // GriefDefender
    compileOnly("com.github.bloodmc:GriefDefenderAPI:b956577866")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    shadowJar {
        archiveClassifier.set("all")
    }
}