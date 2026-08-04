package cat.mey.platform.api.auth.routes.login.data.types.responses

import cat.mey.platform.core.data.types.result.ApiResult
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
sealed interface LoginResponseBody : ApiResult {
    @Serializable
    data class Success(val token: String) : ApiResult.Success(), LoginResponseBody

    @Serializable
    abstract class Failure : ApiResult.Error(), LoginResponseBody {
        @Serializable
        object InvalidPassword : Failure() {
            override val translationKey = "error.login.invalid_password"
            override val statusCode = HttpStatusCode.BadRequest
        }

        @Serializable
        object AccountNotFound : Failure() {
            override val translationKey = "error.login.account_not_found"
            override val statusCode = HttpStatusCode.NotFound
        }
    }

}