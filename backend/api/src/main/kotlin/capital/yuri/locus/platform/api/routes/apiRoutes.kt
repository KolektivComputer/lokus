package capital.yuri.locus.platform.api.routes

import capital.yuri.locus.platform.api.auth.routes.authRoutes
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.apiRoutes() {
    route("/api") {
        authRoutes()
    }
}
