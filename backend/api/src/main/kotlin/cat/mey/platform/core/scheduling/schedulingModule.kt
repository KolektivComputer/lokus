package cat.mey.platform.core.scheduling

import cat.mey.platform.core.scheduling.services.SchedulerService
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val schedulingModule = module {
    single<SchedulerService>()
}