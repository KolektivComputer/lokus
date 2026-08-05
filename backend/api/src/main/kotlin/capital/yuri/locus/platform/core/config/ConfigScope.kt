package capital.yuri.locus.platform.core.config

/** Who owns the config — affects path layout under root or extension id. */
enum class ConfigScope {
    /** Instance-wide config (CONFIG_ROOT or /locus/config). */
    Root,

    /** Per-extension config (…/ext/<extensionId>/…). */
    Extension,
}
