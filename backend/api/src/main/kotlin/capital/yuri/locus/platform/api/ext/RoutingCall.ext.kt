package capital.yuri.locus.platform.api.ext

import capital.yuri.locus.platform.core.data.types.result.ApiResult
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall

suspend fun RoutingCall.respondResult(apiResult: ApiResult) {
    respond(apiResult.statusCode, apiResult)
}
