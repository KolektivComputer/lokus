package capital.yuri.locus.platform.core.scheduling

import capital.yuri.locus.platform.core.scheduling.services.SchedulerService
import org.koin.dsl.module

val schedulingModule = module {
    single { SchedulerService() }
}
