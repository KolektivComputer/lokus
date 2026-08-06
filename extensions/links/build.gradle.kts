plugins {
    id("capital.yuri.locus.kotlin-library")
    id("capital.yuri.locus.config-resources")
    id("capital.yuri.locus.extension")
    alias(libs.plugins.koin.compiler)
}

dependencies {
    // Thin jar: core/koin/exposed stay on the host classpath (parent ClassLoader).
    compileOnly(project(":backend:common"))
    compileOnly(project(":backend:core"))

    compileOnly(platform(libs.koin.bom))
    compileOnly(libs.bundles.koin)
    compileOnly(libs.bundles.kotlinx)
    compileOnly(libs.bundles.logging)
    compileOnly(libs.bundles.database)
    compileOnly(libs.bundles.ktor)
}

locusConfigResources {
    enabled.set(true)
}

locusExtension {
    providers.add("capital.yuri.locus.extensions.links.LinksExtensionProvider")
    archiveBaseName.set("locus-extension-links")
}

koinCompiler {
    compileSafety = false
    strictSafety = false
}
