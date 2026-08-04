@file:OptIn(ExperimentalUuidApi::class)

package cat.mey.platform.core.auth.data.entities

import cat.mey.platform.core.auth.data.tables.SessionsTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Session(id: EntityID<Uuid>) : UuidEntity(id) {
    var account by Account referencedOn SessionsTable.account
        private set

    var name by SessionsTable.name

    companion object : UuidEntityClass<Session>(SessionsTable) {
        fun create(account: Account) = new {
            this.account = account
        }

        fun byAccount(accountId: Uuid) = find {
            SessionsTable.account eq accountId
        }
    }

    fun api(active: Boolean) = Api(
        id = id.value,
        name = name,
        active = active,
    )

    @Serializable
    data class Api(
        val id: Uuid,
        val name: String?,
        val active: Boolean
    )
}