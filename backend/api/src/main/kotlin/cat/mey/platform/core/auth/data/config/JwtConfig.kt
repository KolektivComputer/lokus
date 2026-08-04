package cat.mey.platform.core.auth.data.config

import kotlinx.serialization.Serializable

@Serializable
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
)
