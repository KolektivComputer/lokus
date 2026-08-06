@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.extensions.links.data.entities

import capital.yuri.locus.extensions.links.data.tables.LinkPageEntriesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LinkPageEntry(id: EntityID<Uuid>) : UuidEntity(id) {
    var link by Link referencedOn LinkPageEntriesTable.link
        private set
    var page by LinkPage referencedOn LinkPageEntriesTable.page
        private set

    var enabled by LinkPageEntriesTable.enabled
    var icon by LinkPageEntriesTable.icon

    val createdAt by LinkPageEntriesTable.createdAt
    var updatedAt by LinkPageEntriesTable.updatedAt

    companion object : UuidEntityClass<LinkPageEntry>(LinkPageEntriesTable) {
        fun new(link: Link, page: LinkPage): LinkPageEntry = new {
            this.link = link
            this.page = page
        }
    }
}
