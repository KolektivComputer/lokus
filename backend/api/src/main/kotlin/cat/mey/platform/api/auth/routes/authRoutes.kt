package cat.mey.platform.api.auth.routes

import cat.mey.platform.api.auth.routes.login.loginRoutes
import cat.mey.platform.api.auth.routes.password.passwordRoutes
import cat.mey.platform.api.auth.routes.sessions.sessionsRoutes
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.authRoutes() {
    route("/auth") {
        loginRoutes()
        passwordRoutes()

        authenticate("api-jwt") {
            sessionsRoutes()
        }
    }
}