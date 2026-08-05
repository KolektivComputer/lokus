package capital.yuri.locus.platform.core.scheduling

import org.koin.core.component.KoinComponent
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle

/**
 * Resolves Quartz jobs from Koin by the job's concrete class.
 * Dynamic lookup — not expressible as a static get&lt;Job&gt;() for compileSafety.
 */
class KoinJobFactory :
    JobFactory,
    KoinComponent {
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        val jobClass = bundle.jobDetail.jobClass.kotlin
        return getKoin().get(clazz = jobClass)
    }
}
