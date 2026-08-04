package capital.yuri.locus.platform.core.scheduling.services

import capital.yuri.locus.platform.core.scheduling.KoinJobFactory
import capital.yuri.locus.platform.core.scheduling.RunOnceLaterJobDsl
import org.koin.core.component.KoinComponent
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory

class SchedulerService : KoinComponent {
    val schedulerFactory = StdSchedulerFactory()
    val scheduler: Scheduler = schedulerFactory.scheduler

    init {
        scheduler.setJobFactory(KoinJobFactory())
        scheduler.start()
    }

    inline fun <reified TJob : Job> scheduleRunOnceLaterJob(block: RunOnceLaterJobDsl<TJob>.() -> Unit) {
        RunOnceLaterJobDsl(TJob::class.java).apply(block).schedule(scheduler)
    }
}
