package capital.yuri.locus.platform.core.db

import capital.yuri.locus.platform.core.db.services.DatabaseMigrationService
import capital.yuri.locus.platform.core.db.services.DatabaseService
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val dbModule = module {
    single<DatabaseService>()
    single<DatabaseMigrationService>()
}
