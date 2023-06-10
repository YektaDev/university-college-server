plugins {
    kotlin("jvm") version "1.8.20"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.yekta"
version = "1.0-SNAPSHOT"

repositories.mavenCentral()

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}
