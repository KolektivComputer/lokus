package cat.mey.platform.core.config

/**
 * Marks a serializable config class and describes where it lives on disk.
 *
 * Root configs:     CONFIG_ROOT/<name>.jsonc
 * Extension configs: CONFIG_ROOT/ext/<extensionId>/<name>.jsonc
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigFile(
    /** File name without extension (e.g. "database" -> database.jsonc). */
    val name: String,
    val type: ConfigType = ConfigType.Root,
    /**
     * Only used when [type] is [ConfigType.Extension].
     * When empty, callers must pass extensionId to [cat.mey.platform.core.config.services.ConfigService.config].
     */
    val extensionId: String = "",
)
