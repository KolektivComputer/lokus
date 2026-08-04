package cat.mey.platform.api.routes

import cat.mey.platform.api.auth.routes.authRoutes
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.apiRoutes() {
    route("/api") {
        authRoutes()
    }
}