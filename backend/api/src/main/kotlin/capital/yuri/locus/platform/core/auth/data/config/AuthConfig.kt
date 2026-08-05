package capital.yuri.locus.platform.core.auth.data.config

import capital.yuri.locus.platform.core.config.Config
import capital.yuri.locus.platform.core.config.ConfigLocation
import capital.yuri.locus.platform.core.config.ConfigScope
import kotlinx.serialization.Serializable

@Config(name = "auth", scope = ConfigScope.Root, location = ConfigLocation.File)
@Serializable
data class AuthConfig(val argon: ArgonConfig = ArgonConfig(), val jwt: JwtConfig = JwtConfig())
