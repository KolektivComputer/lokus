plugins {
    id("capital.yuri.locus.kotlin-library")
    id("capital.yuri.locus.config-resources")
    alias(libs.plugins.koin.compiler)
}

dependencies {
    api(project(":backend:common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.database)
    implementation(libs.bundles.ktor)
    implementation(libs.clikt)
    implementation(libs.argon2)
    implementation(libs.quartz)
}

locusConfigResources {
    enabled.set(true)
}

koinCompiler {
    compileSafety = false
    strictSafety = false
}
