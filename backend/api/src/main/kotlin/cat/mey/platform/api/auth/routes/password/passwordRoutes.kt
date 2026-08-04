package cat.mey.platform.api.auth.routes.password

import cat.mey.platform.api.auth.routes.password.data.types.requests.RequestPasswordResetRequestBody
import cat.mey.platform.api.auth.routes.password.data.types.requests.ResetPasswordRequestBody
import cat.mey.platform.api.auth.routes.password.data.types.requests.UpdatePasswordRequestBody
import cat.mey.platform.api.auth.routes.password.data.types.responses.RequestPasswordResetResponseBody
import cat.mey.platform.api.auth.routes.password.data.types.responses.ResetPasswordResponseBody
import cat.mey.platform.api.ext.respondResult
import cat.mey.platform.core.auth.data.entities.Account
import cat.mey.platform.core.auth.data.entities.PasswordResetRequest
import cat.mey.platform.core.auth.data.types.session.ActiveSession
import cat.mey.platform.core.auth.services.AccountAuthenticationService
import cat.mey.platform.core.auth.services.Argon2Service
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
                        .NotFound(body.email)
                )
                return@post
            }

            accounts.requestPasswordResetSuspend(account)

            call.respondResult(RequestPasswordResetResponseBody.Success)
        }

        post("/reset") {
            val body = call.receive<ResetPasswordRequestBody>()

            val request = suspendTransaction {
                PasswordResetRequest.findByToken(body.token)
            } ?: run {
                call.respondResult(ResetPasswordResponseBody.Failure.InvalidToken)
                return@post
            }

            if (request.expired != null) {
                call.respondResult()
                return@post
            }

            when (val result = accounts.resetPassword(body.password, body.token)) {
                is AccountAuthenticationService.ResetPasswordResult
            }
        }
    }
}