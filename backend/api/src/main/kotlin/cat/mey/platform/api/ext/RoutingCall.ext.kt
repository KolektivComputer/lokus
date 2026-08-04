package cat.mey.platform.api.ext

import cat.mey.platform.core.data.types.result.ApiResult
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall

suspend fun RoutingCall.respondResult(apiResult: ApiResult) {
    respond(apiResult.statusCode, apiResult)
}