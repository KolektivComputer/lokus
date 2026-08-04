package cat.mey.platform.core.auth.data.config

import kotlinx.serialization.Serializable

@Serializable
data class AuthConfig(
    val argon: ArgonConfig,
    val jwt: JwtConfig,
) {
}