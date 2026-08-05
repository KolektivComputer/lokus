package capital.yuri.locus.platform.core.stats.data.types.groups

import kotlinx.serialization.Serializable

@Serializable
data class InstanceStatGroup(
    val uptimeSeconds: Long,
    val startedAtEpochMs: Long,
    val version: BuildVersion?,
)

@Serializable
data class BuildVersion(
    val tag: String? = null,
    val commit: String? = null,
    val dirty: Boolean = false,
    val updateUrl: String? = null,
)
