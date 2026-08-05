package capital.yuri.locus.platform.core.auth.jobs

import capital.yuri.locus.platform.core.auth.services.SessionService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class DeactivateSessionsJob :
    Job,
    KoinComponent {
    private val sessionService by inject<SessionService>()
    private val logger = LoggerFactory.getLogger(DeactivateSessionsJob::class.java)

    override fun execute(context: JobExecutionContext?) {
        val inactiveSessions = sessionService.getInactiveSessionIds()
        if (inactiveSessions.isEmpty()) return

        logger.info("Got ${inactiveSessions.size} inactive sessions, removing them from memory pool.")
        inactiveSessions.forEach(sessionService::deactivateSession)
    }

    companion object : KoinComponent {
        // todo: run every 30 minutes
    }
}
