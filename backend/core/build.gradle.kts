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
    implementation(libs.bundles.ktor)
    implementation(libs.clikt)
    implementation(libs.argon2)
    implementation(libs.quartz)
}

locusConfigResources {
    enabled.set(true)
}

/*
 * Source lives under :backend:api until the tree is moved with:
 *
 *   mkdir -p backend/core/src/main/kotlin/capital/yuri/locus/platform
 *   git mv backend/api/src/main/kotlin/capital/yuri/locus/platform/core \
 *          backend/core/src/main/kotlin/capital/yuri/locus/platform/
 *
 * After that move, api keeps only platform.api.*, platform.links.*, main.kt.
 */
