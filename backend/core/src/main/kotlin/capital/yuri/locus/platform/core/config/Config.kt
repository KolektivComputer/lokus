package capital.yuri.locus.platform.core.config

/**
 * Marks a serializable config class and how it is resolved.
 *
 * File (default):
 *   Root:      CONFIG_ROOT/<name>.{jsonc,json}
 *   Extension: CONFIG_ROOT/ext/<extensionId>/<name>.{jsonc,json}
 *
 * Resource:
 *   Root:      classpath:/locus/config/<name>.{jsonc,json}
 *   Extension: classpath:/locus/config/ext/<extensionId>/<name>.{jsonc,json}
 *
 * Defaults (when writing missing File configs):
 *   classpath:/locus/config/defaults/<name>.{jsonc,json}
 *   classpath:/locus/config/defaults/ext/<extensionId>/<name>.{jsonc,json}
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(
    /** Logical name without extension (e.g. "database"). */
    val name: String,
    val scope: ConfigScope = ConfigScope.Root,
    val location: ConfigLocation = ConfigLocation.File,
    /**
     * Only used when [scope] is [ConfigScope.Extension].
     * When empty, callers must pass extensionId to [ConfigService.config].
     */
    val extensionId: String = "",
)
