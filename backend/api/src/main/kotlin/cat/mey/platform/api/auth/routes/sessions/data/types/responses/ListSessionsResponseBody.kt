package cat.mey.platform.api.auth.routes.sessions.data.types.responses

import cat.mey.platform.core.auth.data.entities.Session
import cat.mey.platform.core.data.types.result.ApiResult
import kotlinx.serialization.Serializable

@Serializable
sealed interface ListSessionsResponseBody : ApiResult {
    @Serializable
    data class Success(
        val sessions: List<Session.Api>
    ) : ApiResult.Success(), ListSessionsResponseBody

}