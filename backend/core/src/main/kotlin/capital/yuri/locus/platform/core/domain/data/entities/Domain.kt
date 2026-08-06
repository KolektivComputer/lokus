@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.core.domain.data.entities

import capital.yuri.locus.platform.core.auth.data.entities.Account
import capital.yuri.locus.platform.core.domain.data.tables.DomainsTable
import capital.yuri.locus.platform.core.domain.data.types.EndpointId
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Domain(id: EntityID<Uuid>) : UuidEntity(id) {
    var account by Account referencedOn DomainsTable.account
    var fqdn by DomainsTable.fqdn
    var verifiedAt by DomainsTable.verifiedAt
    var verificationToken by DomainsTable.verificationToken
    private var endpoint by DomainsTable.endpoint

    var endpointId: EndpointId?
        get() = endpoint?.let { EndpointId(it) }
        set(value) {
            endpoint = value?.endpointId
        }

    companion object : UuidEntityClass<Domain>(DomainsTable) {
        fun findByFqdn(fqdn: String) = find {
            (DomainsTable.fqdn eq fqdn)
        }.firstOrNull()

        fun create(account: Account, fqdn: String, verificationToken: String) = new {
            this.account = account
            this.fqdn = fqdn
            this.verificationToken = verificationToken
        }
    }

    @Serializable
    data class Api(val account: Account.Api, val fqdn: String, val endpoint: EndpointId?)
}
