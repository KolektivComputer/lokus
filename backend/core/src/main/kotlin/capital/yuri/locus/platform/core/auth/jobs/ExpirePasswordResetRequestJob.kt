@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.core.auth.jobs

import capital.yuri.locus.platform.core.auth.data.entities.PasswordResetRequest
import capital.yuri.locus.platform.core.scheduling.services.SchedulerService
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.quartz.Job
import org.quartz.JobExecutionContext
import kotlin.collections.set
import kotlin.getValue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExpirePasswordResetRequestJob : Job {
    override fun execute(context: JobExecutionContext) {
        val requestId = context.mergedJobDataMap[REQUEST_ID_KEY]
            ?.let { Uuid.parseOrNull(it.toString()) }
            ?: return

        transaction {
            PasswordResetRequest.findById(requestId)?.expired = Clock.System.now()
        }
    }

    companion object : KoinComponent {
        const val GROUP_NAME = "ExpirePasswordResetRequest"
        const val REQUEST_ID_KEY = "requestId"

        val schedulerService by inject<SchedulerService>()

        fun dispatch(requestId: Uuid) {
            schedulerService.scheduleRunOnceLaterJob<ExpirePasswordResetRequestJob> {
                delay = 1.hours
                identity("expirePasswordResetRequest[requestId=$requestId]", GROUP_NAME)
                jobData[REQUEST_ID_KEY] = requestId
            }
        }
    }
}
