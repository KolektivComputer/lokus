package capital.yuri.locus.platform.core.db.services

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DatabaseMigrationService : KoinComponent {
    private val databaseService by inject<DatabaseService>()
    private val tableRegistry by inject<TableRegistryService>()

    val logger: Logger = LoggerFactory.getLogger(DatabaseMigrationService::class.java)

    /**
     * Apply schema changes for [tables], or every table in [TableRegistryService] when null/empty.
     */
    fun migrate(tables: Collection<Table>? = null) {
        val target = when {
            tables.isNullOrEmpty() -> tableRegistry.all()
            else -> tables.toSet()
        }

        if (target.isEmpty()) {
            logger.warn("No tables registered for migration")
            return
        }

        databaseService.connect()
        logger.info("Beginning migration of {} table(s)", target.size)

        transaction {
            val statements = MigrationUtils.statementsRequiredForDatabaseMigration(*target.toTypedArray())
            if (statements.isEmpty()) {
                logger.info("Schema already up to date")
            } else {
                execInBatch(statements)
                logger.info("Applied {} migration statement(s)", statements.size)
            }
        }
    }
}
