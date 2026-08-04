package capital.yuri.locus.platform.core.scheduling

import org.koin.core.component.KoinComponent
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle
import kotlin.reflect.KClass

class KoinJobFactory :
    JobFactory,
    KoinComponent {
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        val jobClass: KClass<out Job> = bundle.jobDetail.jobClass.kotlin
        // Dynamic class → instance; not a static get<Job>() the compiler can verify.
        @Suppress("UNCHECKED_CAST")
        return getKoin().get(clazz = jobClass) as Job
    }
}
