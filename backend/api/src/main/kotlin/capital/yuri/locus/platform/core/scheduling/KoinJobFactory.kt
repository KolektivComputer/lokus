package capital.yuri.locus.platform.core.scheduling

import org.koin.core.component.KoinComponent
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle

class KoinJobFactory :
    JobFactory,
    KoinComponent {
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        val jobDetail = bundle.jobDetail
        val jobClass = jobDetail.jobClass

        // Resolve the job instance directly from Koin
        return getKoin().get(jobClass.kotlin) as Job
    }
}
