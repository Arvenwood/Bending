plugins {
    java
    kotlin("jvm")
    kotlin("kapt")

    id("com.github.johnrengelman.shadow")
}

group = "pw.dotdash"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")

    // Bending API
    implementation(project(":bending-api"))

    // Shaded
    runtime(kotlin("stdlib-jdk8"))
    runtime(kotlin("reflect"))
    runtime("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    runtime(project(":bending-api"))

    // Sponge
    compileOnly("org.spongepowered:spongeapi:7.1.0")
    kapt("org.spongepowered:spongeapi:7.1.0")
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

artifacts {
    add("archives", shadowJar)
}