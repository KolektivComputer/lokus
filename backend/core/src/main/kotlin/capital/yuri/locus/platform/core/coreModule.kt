package capital.yuri.locus.platform.core

import capital.yuri.locus.platform.core.auth.authModule
import capital.yuri.locus.platform.core.db.dbModule
import capital.yuri.locus.platform.core.domain.domainModule
import capital.yuri.locus.platform.core.extension.extensionModule
import capital.yuri.locus.platform.core.scheduling.schedulingModule
import capital.yuri.locus.platform.core.stats.statsModule
import org.koin.dsl.module

/** Shared platform features. Loaded by [appModule]. */
val coreModule = module {
    includes(
        dbModule,
        authModule,
        schedulingModule,
        domainModule,
        extensionModule,
        statsModule,
    )
}
