import org.gradle.api.plugins.JavaApplication

/**
 * Kotlin JVM application with a configurable main class / app name.
 *
 * ```
 * plugins {
 *   id("capital.yuri.locus.kotlin-application")
 * }
 *
 * locusApplication {
 *   mainClass.set("capital.yuri.locus.cli.MainKt")
 *   applicationName.set("locus-cli")
 * }
 * ```
 */
plugins {
    id("capital.yuri.locus.kotlin-library")
    application
}

interface LocusApplicationExtension {
    val mainClass: Property<String>
    val applicationName: Property<String>
}

val locusApplication =
    extensions.create<LocusApplicationExtension>("locusApplication")

locusApplication.mainClass.convention("MainKt")
locusApplication.applicationName.convention(project.name)

afterEvaluate {
    extensions.configure<JavaApplication>("application") {
        mainClass.set(locusApplication.mainClass)
        applicationName = locusApplication.applicationName.get()
    }
}
