package capital.yuri.locus.platform.api.auth.routes

import capital.yuri.locus.platform.api.auth.routes.login.loginRoutes
import capital.yuri.locus.platform.api.auth.routes.password.passwordRoutes
import capital.yuri.locus.platform.api.auth.routes.sessions.sessionsRoutes
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
