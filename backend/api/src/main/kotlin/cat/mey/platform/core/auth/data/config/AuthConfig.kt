package cat.mey.platform.core.auth.data.config

import cat.mey.platform.core.config.ConfigFile
import cat.mey.platform.core.config.ConfigType
import kotlinx.serialization.Serializable

@ConfigFile(name = "auth", type = ConfigType.Root)
@Serializable
data class AuthConfig(
    val argon: ArgonConfig = ArgonConfig(),
    val jwt: JwtConfig = JwtConfig(),
)
