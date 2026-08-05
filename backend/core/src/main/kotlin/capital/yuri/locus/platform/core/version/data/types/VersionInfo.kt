package capital.yuri.locus.platform.core.version.data.types

import kotlinx.serialization.Serializable

@Serializable
data class CoreVersionInfo(
    val tag: String? = null,
    val commit: String? = null,
    val dirty: Boolean = false,
    /**
     * Optional URL to check for newer releases.
     * Forced to null when [dirty] is true (dev builds).
     */
    val updateUrl: String? = null,
)

@Serializable
data class ComponentVersion(
    val id: String,
    val version: String,
    val kind: ComponentKind,
)

@Serializable
enum class ComponentKind {
    Core,
    Extension,
    WebBundle,
}
