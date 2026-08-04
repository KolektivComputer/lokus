@file:OptIn(ExperimentalUuidApi::class)

package cat.mey.platform.core.auth.data.entities

import cat.mey.platform.core.auth.data.tables.PasswordResetRequestsTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PasswordResetRequest(id: EntityID<Uuid>) : UuidEntity(id) {
    var account by Account referencedOn PasswordResetRequestsTable.account
    var token by PasswordResetRequestsTable.token
    var expired by PasswordResetRequestsTable.expired
    var created by PasswordResetRequestsTable.created

    companion object : UuidEntityClass<PasswordResetRequest>(PasswordResetRequestsTable) {
        fun findByToken(token: String) = find {
            PasswordResetRequestsTable.token eq token
        }.firstOrNull()

        fun create(account: Account, token: String): PasswordResetRequest {
            val preexisting = find {
                (PasswordResetRequestsTable.account eq account.id) and (PasswordResetRequestsTable.expired eq null)
            }

            if (!preexisting.empty()) {
                preexisting.iterator().forEach {
                    it.expired = Clock.System.now()
                }
            }

            return new {
                this.token = token
                this.account = account
            }
        }
    }
}