package capital.yuri.locus.platform.api.routes

import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.statusRoutes() {
    route("/status") {
        get("/responding") {
            call.respond(true)
        }

        get("/version") {
            call.respondText("1.0.0")
            // todo
        }

        get("/instance-stats") {
            call.respondText("Instance stats")
            // todo
        }
    }
}