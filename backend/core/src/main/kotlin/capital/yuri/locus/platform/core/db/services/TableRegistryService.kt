package capital.yuri.locus.platform.core.db.services

import org.jetbrains.exposed.v1.core.Table
import java.util.concurrent.ConcurrentHashMap

/**
 * Shared registry of Exposed [Table]s for schema migration.
 *
 * Core modules and extensions call [register] during startup / extension load.
 * [DatabaseMigrationService.migrate] reads [all] when no explicit table list is passed.
 */
class TableRegistryService {
    private val tables = ConcurrentHashMap.newKeySet<Table>()

    fun register(vararg tables: Table) {
        this.tables.addAll(tables.toList())
    }

    fun register(tables: Collection<Table>) {
        this.tables.addAll(tables)
    }

    fun unregister(vararg tables: Table) {
        this.tables.removeAll(tables.toSet())
    }

    fun all(): Set<Table> = tables.toSet()

    fun clear() {
        tables.clear()
    }
}
