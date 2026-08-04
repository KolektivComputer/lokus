package capital.yuri.locus.platform.api.auth.routes.password.data.types.requests

import kotlinx.serialization.Serializable

@Serializable
data class RequestPasswordResetRequestBody(
    val email: String,
    // todo: csrf
)
