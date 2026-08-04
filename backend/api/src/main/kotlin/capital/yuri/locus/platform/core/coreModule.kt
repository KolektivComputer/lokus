package capital.yuri.locus.platform.core

import capital.yuri.locus.platform.core.config.configModule
import capital.yuri.locus.platform.core.db.dbModule
import capital.yuri.locus.platform.core.scheduling.schedulingModule
import org.koin.dsl.module

val coreModule = module {
    includes(dbModule, schedulingModule, configModule)
}
