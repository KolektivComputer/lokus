plugins {
    id("capital.yuri.locus.kotlin-application")
}

dependencies {
    implementation(project(":backend:common"))
    implementation(project(":backend:core"))

    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(libs.clikt)
}

locusApplication {
    mainClass.set("capital.yuri.locus.cli.MainKt")
    applicationName.set("locus-cli")
}
