@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.api.auth

import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import capital.yuri.locus.platform.core.auth.services.SessionService
import capital.yuri.locus.platform.core.config.services.ConfigService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import org.koin.ktor.ext.inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun Application.configureAuth() {
    val configService by inject<ConfigService>()
    val config by configService.config<AuthConfig>()

    val sessionService by inject<SessionService>()

    install(Authentication) {
        jwt("api-jwt") {
            realm = config.jwt.realm

            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.jwt.secret))
                    .withAudience(config.jwt.audience)
                    .withIssuer(config.jwt.issuer)
                    .build(),
            )

            validate { credential ->
                val sessionId = credential.payload.getClaim("id").asString()?.let {
                    Uuid.parseOrNull(it)
                } ?: return@validate null

                val session = sessionService.getSessionSuspend(sessionId)
                    ?: return@validate null

                return@validate sessionService.ensureActiveSession(session, credential.payload)
            }
        }
    }
}
