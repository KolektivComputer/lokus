package cat.mey.platform.core.db.services

import cat.mey.core.auth.data.tables.AccountsTable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DatabaseMigrationService : KoinComponent {
    private val databaseService by inject<DatabaseService>()

    val logger: Logger = LoggerFactory.getLogger(DatabaseMigrationService::class.java)
    val tables = setOf(
        AccountsTable,
    )

    fun migrate() {
        databaseService.connect()
        logger.info("Beginning migration of database schema")

        transaction {

            // todo: make this a subcommand on clikt or something
            val statements = MigrationUtils.statementsRequiredForDatabaseMigration(*tables.toTypedArray())
            execInBatch(statements)
        }
    }
}