package capital.yuri.locus.platform.core.auth.services

import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import capital.yuri.locus.platform.core.auth.data.entities.Session
import capital.yuri.locus.platform.core.config.services.ConfigService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaInstant
import kotlin.uuid.ExperimentalUuidApi

class JwtService : KoinComponent {
    private val configService: ConfigService by inject()
    private val authConfig by configService.config<AuthConfig>()

    @OptIn(ExperimentalUuidApi::class)
    fun token(session: Session): String = JWT.create()
        .withAudience(authConfig.jwt.audience)
        .withIssuer(authConfig.jwt.issuer)
        .withClaim("id", session.id.value.toString())
        .withExpiresAt((Clock.System.now() + 7.days).toJavaInstant())
        .sign(Algorithm.HMAC256(authConfig.jwt.secret))

}
