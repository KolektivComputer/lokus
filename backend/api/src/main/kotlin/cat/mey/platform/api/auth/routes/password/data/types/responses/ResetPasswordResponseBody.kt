package cat.mey.platform.api.auth.routes.password.data.types.responses

import cat.mey.platform.core.data.types.result.ApiResult
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Instant

@Serializable
sealed interface ResetPasswordResponseBody : ApiResult {

    @Serializable
    object Success : ApiResult.Success(), ResetPasswordResponseBody

    @Serializable
    abstract class Failure : ApiResult.Error(), ResetPasswordResponseBody {
        @Serializable
        object InvalidToken : Failure() {
            override val translationKey = "error.reset_password.invalid_token"

            @Transient
            override val statusCode = HttpStatusCode.BadRequest
        }

        @Serializable
        class RequestExpired private constructor() : Failure() {
            override val translationKey = "error.reset_password.request_expired"

            @Transient
            override val statusCode = HttpStatusCode.Unauthorized

            companion object {
                operator fun invoke(expiredAt: Instant): RequestExpired {
                    return RequestExpired().apply {
                        translationValues["expired_timestamp"] = expiredAt.toString()
                    }
                }
            }
        }
    }
}