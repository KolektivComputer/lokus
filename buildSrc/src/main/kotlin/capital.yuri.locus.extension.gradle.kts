/**
 * Marks a project as a Locus extension and generates the ServiceLoader file:
 *
 *   META-INF/services/capital.yuri.locus.platform.core.extension.ExtensionProvider
 *
 * ```
 * plugins {
 *   id("capital.yuri.locus.extension")
 * }
 *
 * locusExtension {
 *   providers.add("capital.yuri.locus.extensions.links.LinksExtensionProvider")
 *   archiveBaseName.set("locus-extension-links")
 * }
 * ```
 */
interface LocusExtensionExtension {
    /** Fully-qualified ExtensionProvider implementation class names. */
    val providers: ListProperty<String>

    /** Optional JAR base name (default: project name). */
    val archiveBaseName: Property<String>
}

val locusExtension =
    extensions.create<LocusExtensionExtension>("locusExtension")

locusExtension.archiveBaseName.convention(project.name)

val generatedExtensionServices =
    layout.buildDirectory.dir("generated/extension-services")

val generateExtensionServices by tasks.registering {
    val providerClasses = locusExtension.providers
    val outputDir = generatedExtensionServices

    inputs.property("providers") {
        providerClasses.get()
    }
    outputs.dir(outputDir)

    doLast {
        val classes = providerClasses.get().map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        check(classes.isNotEmpty()) {
            "locusExtension.providers must list at least one ExtensionProvider FQCN"
        }

        val servicesDir = outputDir.get().asFile.resolve("META-INF/services")
        servicesDir.mkdirs()
        val servicesFile = servicesDir.resolve(
            "capital.yuri.locus.platform.core.extension.ExtensionProvider",
        )
        servicesFile.writeText(classes.joinToString(separator = "\n", postfix = "\n"))
    }
}

plugins.withId("java") {
    extensions.configure<SourceSetContainer>("sourceSets") {
        named("main") {
            resources.srcDir(generatedExtensionServices)
        }
    }
    tasks.named("processResources") {
        dependsOn(generateExtensionServices)
    }
    tasks.named<Jar>("jar") {
        dependsOn(generateExtensionServices)
        archiveBaseName.set(locusExtension.archiveBaseName)
    }
}
