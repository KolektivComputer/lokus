package capital.yuri.locus.platform.api.routes

import capital.yuri.locus.platform.core.stats.data.types.StatisticGroupId
import capital.yuri.locus.platform.core.stats.data.types.results.GetStatGroupResult
import capital.yuri.locus.platform.core.stats.services.StatsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.statusRoutes() {
    val statsService by inject<StatsService>()

    route("/status") {
        get("/responding") {
            call.respond(true)
        }

        get("/version") {
            when (val result = statsService.getGroup(StatisticGroupId.Instance)) {
                is GetStatGroupResult.SuccessInstance ->
                    call.respond(result.data.version ?: mapOf("tag" to null, "commit" to null))

                else ->
                    call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to "version unavailable"))
            }
        }

        /** List available statistic group ids. */
        get("/stats") {
            call.respond(statsService.listGroupIds().map { it.value })
        }

        /** Full snapshot of every registered group. */
        get("/stats/all") {
            call.respond(statsService.getAll())
        }

        /** Single group by id (instance | runtime | database | scheduler | extension…). */
        get("/stats/{groupId}") {
            val groupId = call.parameters["groupId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, GetStatGroupResult.NotFound(StatisticGroupId("")))

            val result = statsService.getGroup(StatisticGroupId(groupId))
            val status = when (result) {
                is GetStatGroupResult.NotFound -> HttpStatusCode.NotFound
                is GetStatGroupResult.Failed -> HttpStatusCode.InternalServerError
                else -> HttpStatusCode.OK
            }
            call.respond(status, result)
        }
    }
}
