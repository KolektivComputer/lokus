package cat.mey.platform.core.cli.commands

import cat.mey.platform.core.cli.CommandLine
import cat.mey.platform.core.db.createDbModule
import cat.mey.platform.core.db.services.DatabaseMigrationService
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class MigrateCliktCommand : PlatformCliktCommand("migrate") {
    // todo: dry-run, export SQL, apply
    override suspend fun run() {
        val koinApp = startKoin {
            modules(createDbModule(CommandLine(configDirectory)))
        }

        try {
            koinApp.koin.get<DatabaseMigrationService>().migrate()
        } finally {
            stopKoin()
        }
    }
}