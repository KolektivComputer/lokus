package capital.yuri.locus.platform.core.scheduling

import org.quartz.Job
import org.quartz.JobBuilder.newJob
import org.quartz.JobDataMap
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.toJavaInstant

data class JobIdentity(val identity: String, val group: String)

class RunOnceLaterJobDsl<T : Job>(val jobType: Class<out T>) {
    var identity: JobIdentity? = null
    var delay: Duration = Duration.ZERO
    val jobData: JobDataMap = JobDataMap()

    fun identity(identity: String, group: String) {
        this.identity = JobIdentity(identity, group)
    }

    fun schedule(scheduler: Scheduler) {
        val jobDetailBuilder = newJob(jobType)
        identity?.let { jobDetailBuilder.withIdentity(it.identity, it.group) }
        jobDetailBuilder.usingJobData(jobData)

        val triggerBuilder = newTrigger()
        identity?.let { triggerBuilder.withIdentity(it.identity) }
        triggerBuilder.startAt((Clock.System.now() + delay).toJavaInstant())

        scheduler.scheduleJob(jobDetailBuilder.build(), triggerBuilder.build())
    }
}
