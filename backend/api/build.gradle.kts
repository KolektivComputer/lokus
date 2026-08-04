//import org.gradle.kotlin.dsl.detektPlugins

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    application

    alias(libs.plugins.koin.compiler)
//    alias(libs.plugins.detekt)
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

//    detektPlugins(libs.detekt.ktlint.wrapper)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("capital.yuri.locus.MainKt")
}

//detekt {
//    toolVersion = "2.0.0-alpha.5"
//    config.setFrom(file("../../config/detekt/detekt.yml"))
//    buildUponDefaultConfig = true
//}

tasks.test {
    useJUnitPlatform()
}
