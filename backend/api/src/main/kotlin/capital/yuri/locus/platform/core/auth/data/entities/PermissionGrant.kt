@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.core.auth.data.entities

import capital.yuri.locus.platform.core.auth.data.tables.PermissionGrantsTable
import capital.yuri.locus.platform.core.auth.data.types.permission.Permissions
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import java.util.EnumSet
import kotlin.uuid.ExperimentalUuidApi

class PermissionGrant(id: EntityID<CompositeID>) : CompositeEntity(id) {
    var account by Account referencedOn PermissionGrantsTable.account
        private set
    var permission by PermissionGrantsTable.permission
        private set

    companion object : CompositeEntityClass<PermissionGrant>(PermissionGrantsTable) {
        fun grant(account: Account, permission: Permissions) =
            find {
                (PermissionGrantsTable.account eq account.id) and
                        (PermissionGrantsTable.permission eq permission)
            }.firstOrNull() ?: new {
                this.account = account
                this.permission = permission
            }

        fun grantAll(account: Account, permissions: EnumSet<Permissions> = EnumSet.allOf(Permissions::class.java)) {
            permissions.forEach { grant(account, it) }
        }

        fun revoke(account: Account, permission: Permissions) {
            find {
                (PermissionGrantsTable.account eq account.id) and
                        (PermissionGrantsTable.permission eq permission)
            }.firstOrNull()?.delete()
        }

        fun revokeAll(account: Account, permissions: EnumSet<Permissions> = EnumSet.allOf(Permissions::class.java)) {
            permissions.forEach { revoke(account, it) }
        }

        fun check(account: Account, permission: Permissions) =
            find {
                (PermissionGrantsTable.account eq account.id) and
                        (PermissionGrantsTable.permission eq permission)
            }.empty().not()
    }
}
