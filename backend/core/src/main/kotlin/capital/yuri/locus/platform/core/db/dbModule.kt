package capital.yuri.locus.platform.core.db

import capital.yuri.locus.platform.core.auth.data.tables.AccountsTable
import capital.yuri.locus.platform.core.auth.data.tables.PasswordResetRequestsTable
import capital.yuri.locus.platform.core.auth.data.tables.PermissionGrantsTable
import capital.yuri.locus.platform.core.auth.data.tables.SessionsTable
import capital.yuri.locus.platform.core.db.services.DatabaseMigrationService
import capital.yuri.locus.platform.core.db.services.DatabaseService
import capital.yuri.locus.platform.core.db.services.TableRegistryService
import capital.yuri.locus.platform.core.domain.data.tables.DomainsTable
import org.koin.dsl.module

val dbModule = module {
    single { TableRegistryService() }
    single { DatabaseService() }
    single { DatabaseMigrationService() }
}

/** Platform tables owned by core (not extension-owned). */
fun TableRegistryService.registerCoreTables() {
    register(
        AccountsTable,
        PermissionGrantsTable,
        SessionsTable,
        PasswordResetRequestsTable,
        DomainsTable,
    )
}
