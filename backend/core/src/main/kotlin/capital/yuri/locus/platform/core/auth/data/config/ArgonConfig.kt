package capital.yuri.locus.platform.core.auth.data.config

import kotlinx.serialization.Serializable

@Serializable
data class ArgonConfig(val saltRounds: Int = 10)
