package cat.mey.platform.api.auth.routes.password.data.types.responses

import cat.mey.platform.core.data.types.result.ApiResult
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface RequestPasswordResetResponseBody : ApiResult {
    @Serializable
    object Success : ApiResult.Success(), RequestPasswordResetResponseBody

    @Serializable
    abstract class Failure : ApiResult.Error(), RequestPasswordResetResponseBody {
        @Serializable
        class NotFound private constructor(): Failure() {
            override val translationKey = "error.password_reset.account_not_found"

            @Transient
            override val statusCode = HttpStatusCode.NotFound

            companion object {
                operator fun invoke(email: String) =
                    NotFound().apply {
                        translationValues["email"] = email
                    }
            }
        }
    }
}