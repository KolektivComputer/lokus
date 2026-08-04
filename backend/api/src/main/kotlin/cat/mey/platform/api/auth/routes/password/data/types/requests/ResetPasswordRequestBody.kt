package cat.mey.platform.api.auth.routes.password.data.types.requests

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequestBody(
    val token: String,
    val password: String,
)
