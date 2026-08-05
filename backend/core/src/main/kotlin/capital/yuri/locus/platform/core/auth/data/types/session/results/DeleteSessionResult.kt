package capital.yuri.locus.platform.core.auth.data.types.session.results

import capital.yuri.locus.platform.core.data.types.result.ApiResult
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
sealed interface DeleteSessionResult : ApiResult {
    @Serializable
    object Success : ApiResult.Success(), DeleteSessionResult

    @Serializable
    abstract class Failure :
        ApiResult.Error(),
        DeleteSessionResult {
        object NotFound : Failure() {
            override val translationKey = "error.session.not_found"

            @Transient
            override val statusCode: HttpStatusCode = HttpStatusCode.NotFound
        }

        object Unauthorized : Failure() {
            override val translationKey = "error.session.delete.unauthorized"

            @Transient
            override val statusCode: HttpStatusCode = HttpStatusCode.Unauthorized
        }
    }
}
