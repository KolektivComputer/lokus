package cat.mey.platform.core.db

import cat.mey.api.core.cli.CommandLine
import cat.mey.api.core.config.configModule
import cat.mey.api.core.db.services.DatabaseMigrationService
import cat.mey.api.core.db.services.DatabaseService
import org.koin.dsl.module

val dbModule = module {
    includes(configModule)
    single<DatabaseService>()
    single<DatabaseMigrationService>()
}

fun createDbModule(commandLine: CommandLine) = module {
    single { commandLine }
    includes(dbModule)
}