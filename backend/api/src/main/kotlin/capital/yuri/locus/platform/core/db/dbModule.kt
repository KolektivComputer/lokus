package capital.yuri.locus.platform.core.db

import capital.yuri.locus.platform.core.auth.data.tables.AccountsTable
import capital.yuri.locus.platform.core.auth.data.tables.PasswordResetRequestsTable
import capital.yuri.locus.platform.core.auth.data.tables.PermissionGrantsTable
import capital.yuri.locus.platform.core.auth.data.tables.SessionsTable
import capital.yuri.locus.platform.core.db.services.DatabaseMigrationService
import capital.yuri.locus.platform.core.db.services.DatabaseService
import capital.yuri.locus.platform.core.db.services.TableRegistryService
import capital.yuri.locus.platform.core.domain.data.tables.DomainsTable
import capital.yuri.locus.platform.links.data.tables.LinkPageEntriesTable
import capital.yuri.locus.platform.links.data.tables.LinkPagesTable
import capital.yuri.locus.platform.links.data.tables.LinksTable
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val dbModule = module {
    single { TableRegistryService() }
    single<DatabaseService>()
    single<DatabaseMigrationService>()
}

/** Register platform tables once the Koin graph is live. Call from app startup. */
fun TableRegistryService.registerCoreTables() {
    register(
        AccountsTable,
        PermissionGrantsTable,
        SessionsTable,
        PasswordResetRequestsTable,
        DomainsTable,
        LinksTable,
        LinkPagesTable,
        LinkPageEntriesTable,
    )
}
