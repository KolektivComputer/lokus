package capital.yuri.locus.platform.core

import capital.yuri.locus.platform.core.db.dbModule
import capital.yuri.locus.platform.core.scheduling.schedulingModule
import org.koin.dsl.module

/**
 * Shared infrastructure. Does **not** include config — entry modules must
 * [capital.yuri.locus.platform.core.config.createConfigModule] first so
 * [capital.yuri.locus.platform.core.config.services.ConfigService] is bound
 * with a concrete config directory.
 */
val coreModule = module {
    includes(dbModule, schedulingModule)
}
