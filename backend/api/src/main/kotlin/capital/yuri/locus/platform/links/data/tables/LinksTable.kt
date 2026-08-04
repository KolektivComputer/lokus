@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.links.data.tables

import capital.yuri.locus.platform.core.auth.data.tables.AccountsTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.uuid.ExperimentalUuidApi

object LinksTable : UuidTable("links") {
    val account = reference("account", AccountsTable, onDelete = ReferenceOption.CASCADE)

    val destination = varchar("destination", 2048)

    val shortId = varchar("short_id", 11)
    val vanity = varchar("vanity", 100).nullable()

    val name = varchar("name", 100).nullable()
}
