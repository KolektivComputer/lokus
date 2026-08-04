package capital.yuri.locus.platform.core.db

import capital.yuri.locus.platform.core.cli.CommandLine
import capital.yuri.locus.platform.core.config.configModule
import capital.yuri.locus.platform.core.db.services.DatabaseMigrationService
import capital.yuri.locus.platform.core.db.services.DatabaseService
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val dbModule = module {
    includes(configModule)
    single<DatabaseService>()
    single<DatabaseMigrationService>()
}

fun createDbModule(commandLine: CommandLine) = module {
    single { commandLine }
    includes(dbModule)
}
