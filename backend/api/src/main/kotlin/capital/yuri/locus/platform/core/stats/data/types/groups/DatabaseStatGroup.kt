package capital.yuri.locus.platform.core.stats.data.types.groups

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DatabaseStatGroup {
    @Serializable
    @SerialName("ok")
    data class Ok(
        val latencyMs: Long,
        val jdbcUrlHost: String? = null,
    ) : DatabaseStatGroup

    @Serializable
    @SerialName("unavailable")
    data class Unavailable(
        val message: String,
    ) : DatabaseStatGroup
}
