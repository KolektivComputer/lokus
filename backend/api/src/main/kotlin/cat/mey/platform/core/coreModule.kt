package cat.mey.platform.core

import cat.mey.platform.core.config.configModule
import cat.mey.platform.core.db.dbModule
import cat.mey.platform.core.scheduling.schedulingModule
import org.koin.dsl.module

val coreModule = module {
    includes(dbModule, schedulingModule, configModule)
}