plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    application

    alias(libs.plugins.koin.compiler)
}

group = "capital.yuri"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.database)

    implementation(libs.argon2)
    implementation(libs.quartz)
    implementation(libs.clikt)
}

kotlin {
    jvmToolchain(21)
}

application {
    // Sources live under capital.yuri.locus.platform after the IDE rename
    mainClass.set("capital.yuri.locus.platform.MainKt")
}

// createApiModule(CommandLine) + Quartz job resolution are dynamic; the compiler
// plugin cannot statically prove those graphs and auto-enables strictSafety.
koinCompiler {
    strictSafety.set(false)
}

tasks.test {
    useJUnitPlatform()
}
