@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.extensions.links.data.entities

import capital.yuri.locus.extensions.links.data.tables.LinkPageEntriesTable
import capital.yuri.locus.extensions.links.data.tables.LinkPagesTable
import capital.yuri.locus.platform.core.auth.data.entities.Account
import capital.yuri.locus.platform.core.domain.data.entities.Domain
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LinkPage(id: EntityID<Uuid>) : UuidEntity(id) {
    var account by LinkPagesTable.account
        private set
    var domain by LinkPagesTable.domain
        private set

    val entries by LinkPageEntry referrersOn LinkPageEntriesTable.page

    companion object : UuidEntityClass<LinkPage>(LinkPagesTable) {
        fun new(account: Account, domain: Domain): LinkPage = new {
            this.account = account.id
            this.domain = domain.id
        }
    }
}
