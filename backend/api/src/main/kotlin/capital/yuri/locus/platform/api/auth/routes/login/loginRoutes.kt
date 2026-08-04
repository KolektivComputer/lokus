@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.api.auth.routes.login

import capital.yuri.locus.platform.api.auth.routes.login.data.types.requests.LoginRequestBody
import capital.yuri.locus.platform.api.auth.routes.login.data.types.responses.LoginResponseBody
import capital.yuri.locus.platform.core.auth.data.entities.Session
import capital.yuri.locus.platform.core.auth.data.types.session.results.LoginResult
import capital.yuri.locus.platform.core.auth.services.AccountAuthenticationService
import capital.yuri.locus.platform.core.auth.services.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import kotlin.uuid.ExperimentalUuidApi

fun Route.loginRoutes() {
    route("/login") {
        val accounts by inject<AccountAuthenticationService>()
        val jwt by inject<JwtService>()

        post("/basic") {
            val body = call.receive<LoginRequestBody>()

            when (val result = accounts.login(body.username, body.password)) {
                is LoginResult.Success -> {
                    val account = result.account

                    val session = suspendTransaction {
                        Session.create(account)
                    }

                    val token = jwt.token(session)

                    call.respond(HttpStatusCode.OK, LoginResponseBody.Success(token))
                }

                is LoginResult.Failure -> {
                    when (result) {
                        LoginResult.Failure.NotFound ->
                            call.respond(HttpStatusCode.NotFound, LoginResponseBody.Failure.AccountNotFound)

                        LoginResult.Failure.InvalidPassword ->
                            call.respond(HttpStatusCode.Unauthorized, LoginResponseBody.Failure.InvalidPassword)
                    }
                }
            }
        }
    }
}
