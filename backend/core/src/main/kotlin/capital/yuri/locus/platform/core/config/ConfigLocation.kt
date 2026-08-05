package capital.yuri.locus.platform.core.config

import java.io.File
import java.nio.file.Path
import kotlin.io.path.div

/**
 * Where a config document is loaded from.
 * Each value supplies candidate paths for a given [ConfigResolveContext].
 */
enum class ConfigLocation {
    /**
     * On-disk under the process config directory (watched + reloadable).
     * Tries name.jsonc then name.json.
     */
    File {
        override fun fileCandidates(ctx: ConfigResolveContext): List<File> {
            val dir = when (ctx.scope) {
                ConfigScope.Root -> ctx.configDirectory
                ConfigScope.Extension -> {
                    val id = requireNotNull(ctx.extensionId) { "extensionId required for Extension scope" }
                    ctx.configDirectory / "ext" / id
                }
            }
            return ConfigCodec.EXTENSIONS.map { ext -> (dir / "${ctx.name}.$ext").toFile() }
        }

        override fun resourceCandidates(ctx: ConfigResolveContext): List<String> = emptyList()
    },

    /**
     * Classpath under /locus/config/… (read-only; not watched).
     */
    Resource {
        override fun fileCandidates(ctx: ConfigResolveContext): List<File> = emptyList()

        override fun resourceCandidates(ctx: ConfigResolveContext): List<String> {
            val base = when (ctx.scope) {
                ConfigScope.Root -> "/locus/config/${ctx.name}"
                ConfigScope.Extension -> {
                    val id = requireNotNull(ctx.extensionId) { "extensionId required for Extension scope" }
                    "/locus/config/ext/$id/${ctx.name}"
                }
            }
            return ConfigCodec.EXTENSIONS.map { ext -> "$base.$ext" }
        }
    },
    ;

    abstract fun fileCandidates(ctx: ConfigResolveContext): List<File>

    abstract fun resourceCandidates(ctx: ConfigResolveContext): List<String>

    companion object {
        /** Classpath defaults used when a File config is missing and writeDefault is true. */
        fun defaultResourceCandidates(ctx: ConfigResolveContext): List<String> {
            val base = when (ctx.scope) {
                ConfigScope.Root -> "/locus/config/defaults/${ctx.name}"
                ConfigScope.Extension -> {
                    val id = requireNotNull(ctx.extensionId)
                    "/locus/config/defaults/ext/$id/${ctx.name}"
                }
            }
            return ConfigCodec.EXTENSIONS.map { ext -> "$base.$ext" }
        }
    }
}

data class ConfigResolveContext(
    val name: String,
    val scope: ConfigScope,
    val extensionId: String?,
    val configDirectory: Path,
)
