plugins {
    id("capital.yuri.locus.kotlin-library")
    id("capital.yuri.locus.config-resources")
}

dependencies {
    api(project(":backend:common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.database)
    implementation(libs.clikt)
}

locusConfigResources {
    enabled.set(true)
}
