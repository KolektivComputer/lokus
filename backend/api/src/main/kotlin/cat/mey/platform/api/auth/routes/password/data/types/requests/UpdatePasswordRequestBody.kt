package cat.mey.platform.api.auth.routes.password.data.types.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePasswordRequestBody(
    val password: String
    // todo: csrf
)