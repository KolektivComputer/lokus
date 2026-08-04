package capital.yuri.locus.platform.api.auth.routes.password

import capital.yuri.locus.platform.api.auth.routes.password.data.types.requests.RequestPasswordResetRequestBody
import capital.yuri.locus.platform.api.auth.routes.password.data.types.requests.ResetPasswordRequestBody
import capital.yuri.locus.platform.api.auth.routes.password.data.types.requests.UpdatePasswordRequestBody
import capital.yuri.locus.platform.api.auth.routes.password.data.types.responses.RequestPasswordResetResponseBody
import capital.yuri.locus.platform.api.auth.routes.password.data.types.responses.ResetPasswordResponseBody
import capital.yuri.locus.platform.api.ext.respondResult
import capital.yuri.locus.platform.core.auth.data.entities.Account
import capital.yuri.locus.platform.core.auth.data.types.results.ResetPasswordResult
import capital.yuri.locus.platform.core.auth.data.types.session.ActiveSession
import capital.yuri.locus.platform.core.auth.services.AccountAuthenticationService
import capital.yuri.locus.platform.core.auth.services.Argon2Service
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject

fun Route.passwordRoutes() {
    route("/password") {
        val argon2 by inject<Argon2Service>()
        val accounts by inject<AccountAuthenticationService>()

        authenticate("api-jwt") {
            put("/update") {
                val body = call.receive<UpdatePasswordRequestBody>()

                val session = call.principal<ActiveSession>() ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }

                // todo: check password requirements

                suspendTransaction {
                    session.session.account.passwordHash = argon2.hashPassword(body.password)
                }

                call.respond(HttpStatusCode.OK)
            }
        }

        post("/request-reset") {
            val body = call.receive<RequestPasswordResetRequestBody>()

            val account = suspendTransaction {
                Account.findByEmail(body.email)
            } ?: run {
                call.respondResult(
                    RequestPasswordResetResponseBody
                        .Failure
                        .NotFound(body.email),
                )
                return@post
            }

            accounts.requestPasswordResetSuspend(account)

            call.respondResult(RequestPasswordResetResponseBody.Success)
        }

        post("/reset") {
            val body = call.receive<ResetPasswordRequestBody>()

            when (val result = accounts.resetPasswordSuspend(body.password, body.token)) {
                is ResetPasswordResult.Success -> {
                    call.respondResult(ResetPasswordResponseBody.Success)
                }

                is ResetPasswordResult.Failure.TokenExpired -> {
                    call.respondResult(ResetPasswordResponseBody.Failure.RequestExpired(result.timestamp))
                }

                is ResetPasswordResult.Failure.RequestNotFound -> {
                    call.respondResult(ResetPasswordResponseBody.Failure.InvalidToken)
                }
            }
        }
    }
}
