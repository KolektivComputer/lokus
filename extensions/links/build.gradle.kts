plugins {
    id("capital.yuri.locus.kotlin-library")
    id("capital.yuri.locus.config-resources")
}

dependencies {
    implementation(project(":backend:common"))
    implementation(project(":backend:core"))

    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
}

locusConfigResources {
    enabled.set(true)
}
