package capital.yuri.locus.platform.core.auth.jobs

import capital.yuri.locus.platform.core.auth.data.tables.PasswordResetRequestsTable
import capital.yuri.locus.platform.core.scheduling.services.SchedulerService
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeleteExpiredPasswordResetRequestsJob : Job {
    private val logger: Logger = LoggerFactory.getLogger(DeleteExpiredPasswordResetRequestsJob::class.java)

    override fun execute(context: JobExecutionContext) {
        logger.info("Deleting expired password reset requests.")
        transaction {
            PasswordResetRequestsTable.deleteWhere {
                PasswordResetRequestsTable.expired neq null
            }
        }
    }

    companion object : KoinComponent {
        val schedulerService by inject<SchedulerService>()

        const val GROUP = "CLEANUP"
    }
}
