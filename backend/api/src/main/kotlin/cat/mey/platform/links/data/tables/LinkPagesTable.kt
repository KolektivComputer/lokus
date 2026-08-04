@file:OptIn(ExperimentalUuidApi::class)

package cat.mey.platform.links.data.tables

import cat.mey.platform.core.auth.data.tables.AccountsTable
import cat.mey.platform.core.domain.data.tables.DomainsTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.uuid.ExperimentalUuidApi

object LinkPagesTable : UuidTable("link_pages") {
    val account = reference("account", AccountsTable, onDelete = ReferenceOption.CASCADE)
    val domain = reference("domain", DomainsTable, onDelete = ReferenceOption.CASCADE)
}