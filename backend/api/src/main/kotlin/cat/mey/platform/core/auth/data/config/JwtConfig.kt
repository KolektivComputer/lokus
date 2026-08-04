package cat.mey.platform.core.auth.data.config

import kotlinx.serialization.Serializable

@Serializable
data class JwtConfig(
    val secret: String = "change-me",
    val issuer: String = "overlay",
    val audience: String = "overlay",
    val realm: String = "overlay",
)
