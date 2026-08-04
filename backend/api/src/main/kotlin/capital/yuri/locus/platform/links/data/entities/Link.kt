@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.links.data.entities

import capital.yuri.locus.platform.core.auth.data.entities.Account
import capital.yuri.locus.platform.links.data.tables.LinkPageEntriesTable
import capital.yuri.locus.platform.links.data.tables.LinksTable
import io.ktor.http.Url
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Link(id: EntityID<Uuid>) : UuidEntity(id) {
    var account by Account referencedOn LinksTable.account

    var shortId by LinksTable.shortId
        private set
    var vanity by LinksTable.vanity

    private var destination by LinksTable.destination
    private var name by LinksTable.name

    val pageEntry by LinkPageEntry optionalBackReferencedOn LinkPageEntriesTable.link

    var destinationUrl: Url
        get() = Url(destination)
        set(value) {
            destination = value.toString()
        }

    companion object : UuidEntityClass<Link>(LinksTable) {
        fun findByVanityOrShortId(account: Account, searchTerm: String) = find {
            (LinksTable.account eq account.id) and
                ((LinksTable.vanity eq searchTerm) or (LinksTable.shortId eq searchTerm))
        }.firstOrNull()

        fun findByShortId(account: Account, shortId: String) = find {
            (LinksTable.account eq account.id) and (LinksTable.shortId eq shortId)
        }.firstOrNull()

        fun findByVanity(account: Account, vanity: String) = find {
            (LinksTable.account eq account.id) and (LinksTable.vanity eq vanity)
        }.firstOrNull()

        fun create(account: Account, destination: Url, shortId: String) = new {
            this.destination = destination.toString()
            this.account = account
            this.shortId = shortId
        }
    }
}
