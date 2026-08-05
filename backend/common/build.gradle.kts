plugins {
    id("capital.yuri.locus.kotlin-library")
}

dependencies {
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(libs.koin.core)
}
