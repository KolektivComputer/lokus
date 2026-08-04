package capital.yuri.locus.platform.core.cli.commands

import capital.yuri.locus.platform.core.appModule
import capital.yuri.locus.platform.core.db.services.DatabaseMigrationService
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.requireObject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.nio.file.Path

class MigrateCliktCommand : SuspendingCliktCommand("migrate") {
    val configDirectory by requireObject<Path>()

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
