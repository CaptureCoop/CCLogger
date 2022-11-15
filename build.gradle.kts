import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "org.capturecoop"
version = "1.6.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.capturecoop:CCUtils:1.9.6") //CaptureCoop Common Utils
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}