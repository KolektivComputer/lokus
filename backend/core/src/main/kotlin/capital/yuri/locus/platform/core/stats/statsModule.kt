package capital.yuri.locus.platform.core.stats

import capital.yuri.locus.platform.core.stats.services.StatsService
import org.koin.dsl.module

val statsModule = module {
    single { StatsService() }
}
