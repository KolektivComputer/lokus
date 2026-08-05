/**
 * Shared Kotlin JVM library defaults for locus modules.
 */
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "capital.yuri.locus"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    "testImplementation"(kotlin("test"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
