package capital.yuri.locus.platform.core.cli.commands

import capital.yuri.locus.platform.core.appModule
import capital.yuri.locus.platform.core.db.services.DatabaseMigrationService
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class MigrateCliktCommand : PlatformCliktCommand("migrate") {
    // todo: dry-run, export SQL, apply
    override suspend fun run() {
        val koinApp = startKoin {
            modules(appModule(configDirectory))
        }

        try {
            koinApp.koin.get<DatabaseMigrationService>().migrate()
        } finally {
            stopKoin()
        }
    }
}
