@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.extensions.links.data.tables

import capital.yuri.locus.platform.core.auth.data.tables.AccountsTable
import capital.yuri.locus.platform.core.domain.data.tables.DomainsTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.uuid.ExperimentalUuidApi

object LinkPagesTable : UuidTable("link_pages") {
    val account = reference("account", AccountsTable, onDelete = ReferenceOption.CASCADE)
    val domain = reference("domain", DomainsTable, onDelete = ReferenceOption.CASCADE)
}
