@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.links.data.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

object LinkPageEntriesTable : UuidTable("link_page_entries") {
    val link = reference("link", LinksTable, ReferenceOption.CASCADE).uniqueIndex()
    val page = reference("page", LinkPagesTable, ReferenceOption.CASCADE)

    val enabled = bool("enabled").default(true)
    val icon = varchar("icon", 256).nullable() // todo: enum

    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").nullable().default(null)
}
