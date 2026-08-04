package capital.yuri.locus.platform.api.auth.routes.login.data.types.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestBody(val username: String, val password: String)
