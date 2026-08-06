package capital.yuri.locus.platform.core.stats

import capital.yuri.locus.platform.core.stats.services.StatsService
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val statsModule = module {
    single<StatsService>()
}
