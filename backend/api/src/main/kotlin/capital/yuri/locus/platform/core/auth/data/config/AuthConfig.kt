package capital.yuri.locus.platform.core.auth.data.config

import capital.yuri.locus.platform.core.config.ConfigFile
import capital.yuri.locus.platform.core.config.ConfigType
import kotlinx.serialization.Serializable

@ConfigFile(name = "auth", type = ConfigType.Root)
@Serializable
data class AuthConfig(val argon: ArgonConfig = ArgonConfig(), val jwt: JwtConfig = JwtConfig())
