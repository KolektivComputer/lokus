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
    mainClass.set("capital.yuri.locus.platform.MainKt")
}

/*
 * Koin compiler plugin compile-time safety (A2/A3/A4).
 *
 * compileSafety — validates inject/get against declared modules (KOIN-D002).
 *   Must be false while ConfigService is wired via appModule(Path) and Quartz
 *   resolves Job by Class at runtime — those definitions are not static DSL.
 *
 * strictSafety — only forces the aggregator safety pass to re-run every build
 *   (IC workaround). It does NOT disable KOIN-D002.
 */
koinCompiler {
    compileSafety = false
    strictSafety = false
}

tasks.test {
    useJUnitPlatform()
}
