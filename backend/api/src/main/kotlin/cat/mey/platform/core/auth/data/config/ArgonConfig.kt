package cat.mey.platform.core.auth.data.config

import kotlinx.serialization.Serializable

@Serializable
data class ArgonConfig(
    val saltRounds: Int
)
