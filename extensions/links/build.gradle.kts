plugins {
    id("capital.yuri.locus.kotlin-library")
    id("capital.yuri.locus.config-resources")
    alias(libs.plugins.koin.compiler)
}

dependencies {
    implementation(project(":backend:common"))
    implementation(project(":backend:core"))

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.database)
    implementation(libs.bundles.ktor)
}

locusConfigResources {
    enabled.set(true)
}

koinCompiler {
    compileSafety = false
    strictSafety = false
}
