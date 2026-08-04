@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.api.auth.routes.sessions

import capital.yuri.locus.platform.api.auth.routes.sessions.data.types.responses.ListSessionsResponseBody
import capital.yuri.locus.platform.core.auth.data.types.session.ActiveSession
import capital.yuri.locus.platform.core.auth.services.SessionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import kotlin.uuid.ExperimentalUuidApi

fun Route.sessionsRoutes() {
    route("/sessions") {
        val sessionService by inject<SessionService>()

        get("/all") {
            val session = call.principal<ActiveSession>() ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val sessions = sessionService.getAccountSessionsSuspend(session)
            call.respond(
                HttpStatusCode.OK,
                ListSessionsResponseBody.Success(
                sessions = sessions.map {
                    it.api(sessionService.isActive(it.id.value))
                },
            )
            )
        }

        get("/current") {
            val session = call.principal<ActiveSession>() ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            call.respond(session.session.api(true))
        }

        delete("/current") {
            val session = call.principal<ActiveSession>() ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }

            val result = sessionService.deleteSessionSuspend(session, session.id)
            call.respond(HttpStatusCode.OK, result)
        }
    }
}
