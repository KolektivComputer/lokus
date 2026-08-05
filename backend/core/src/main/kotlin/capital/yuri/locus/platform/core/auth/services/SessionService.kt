package capital.yuri.locus.platform.core.auth.services

import capital.yuri.locus.platform.core.auth.data.entities.Session
import capital.yuri.locus.platform.core.auth.data.types.session.ActiveSession
import capital.yuri.locus.platform.core.auth.data.types.session.results.DeleteSessionResult
import com.auth0.jwt.interfaces.Payload
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SessionService : KoinComponent {
    private val activeSessions: MutableMap<Uuid, ActiveSession> = mutableMapOf()
    private val logger = LoggerFactory.getLogger(SessionService::class.java)

    fun ensureActiveSession(session: Session, payload: Payload): ActiveSession {
        activeSessions.compute(session.id.value) { key, value ->
            if (value == null) {
                logger.info("Creating active session for ${session.id.value}")
                ActiveSession(session, payload)
            } else {
                logger.info("Session ${session.id.value} is already active, updating last active timestamp.")
                value.lastActive = Clock.System.now()
            }

            value
        }

        return activeSessions[session.id.value]!!
    }

    fun getInactiveSessionIds(duration: Duration = 1.hours) = activeSessions.values.filter {
        it.lastActive < (Clock.System.now() - duration)
    }.map { it.session.id.value }

    fun deactivateSession(id: Uuid) {
        activeSessions -= id
    }

    fun isActive(id: Uuid) = activeSessions.containsKey(id)

    suspend fun getSessionSuspend(id: Uuid) = suspendTransaction {
        Session.findById(id)
    }

    suspend fun deleteSessionSuspend(activeSession: ActiveSession, id: Uuid): DeleteSessionResult {
        val session = suspendTransaction { Session.findById(id) } ?: return DeleteSessionResult.Failure.NotFound
        if (activeSession.session.account != session.account) return DeleteSessionResult.Failure.Unauthorized

        activeSessions.remove(id)
        suspendTransaction { session.delete() }
        return DeleteSessionResult.Success
    }

    suspend fun getAccountSessionsSuspend(accountId: Uuid) = suspendTransaction {
        Session.byAccount(accountId)
    }

    suspend fun getAccountSessionsSuspend(activeSession: ActiveSession) = getAccountSessionsSuspend(
        activeSession.accountId,
    )
}
