package capital.yuri.locus.platform.core.auth.data.types.session

import capital.yuri.locus.platform.core.auth.data.entities.Session
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.auth.jwt.JWTPayloadHolder
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class ActiveSession(val session: Session, jwtPayload: Payload) : JWTPayloadHolder(jwtPayload) {
    var lastActive: Instant = Clock.System.now()
    val id get() = session.id.value
    val accountId get() = session.account.id.value
}
