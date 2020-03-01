plugins {
    java
    kotlin("jvm")
    kotlin("kapt")
}

group = "pw.dotdash"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    // Kotlin
    compileOnly(kotlin("stdlib-jdk8"))

    // Bending API
    compileOnly(project(":bending-api"))

    // Sponge
    compileOnly("org.spongepowered:spongeapi:7.1.0")
    kapt("org.spongepowered:spongeapi:7.1.0")
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
}