@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.core.auth.data.entities

import capital.yuri.locus.common.ext.enumSetOf
import capital.yuri.locus.platform.core.auth.data.tables.AccountsTable
import capital.yuri.locus.platform.core.auth.data.tables.PermissionGrantsTable
import capital.yuri.locus.platform.core.auth.data.types.permission.Permissions
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import java.util.EnumSet
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Account(id: EntityID<Uuid>) : UuidEntity(id) {
    var username by AccountsTable.username
    var email by AccountsTable.email
    var passwordHash by AccountsTable.passwordHash

    val permissionGrants by PermissionGrant referrersOn PermissionGrantsTable.account

    val permissions: EnumSet<Permissions> get() = enumSetOf(permissionGrants.map { it.permission })

    companion object : UuidEntityClass<Account>(AccountsTable) {
        fun findByUsernameOrEmail(usernameOrEmail: String) = find {
            (AccountsTable.username eq usernameOrEmail) or (AccountsTable.email eq usernameOrEmail)
        }.firstOrNull()

        fun findByEmail(email: String) = find {
            AccountsTable.email eq email
        }.firstOrNull()
    }

    val api get() = Api(
        username = username,
    )

    @Serializable
    data class Api(val username: String)
}
