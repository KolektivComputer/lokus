package cat.mey.platform.core.extension.data.types

/**
 * Companion contract for extensions so [ExtensionId] is available as a constant
 * for `@ConfigFile(extensionId = ...)` and registry keys.
 */
interface ExtensionCompanion {
    val ID: ExtensionId
}
