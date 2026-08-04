@file:OptIn(ExperimentalUuidApi::class)

package cat.mey.platform.core.auth.data.entities

import cat.mey.platform.core.auth.data.tables.PermissionGrantsTable
import cat.mey.platform.core.auth.data.types.permission.Permissions
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import java.util.EnumSet
import kotlin.uuid.ExperimentalUuidApi

class PermissionGrant(id: EntityID<CompositeID>) : CompositeEntity(id) {
    val account by Account referencedOn PermissionGrantsTable.account
    val permission by PermissionGrantsTable.permission

    companion object : CompositeEntityClass<PermissionGrant>(PermissionGrantsTable) {
        fun grant(account: Account, permission: Permissions): PermissionGrant? {

        }

        fun grantAll(
            account: Account,
            permissions: EnumSet<Permissions> = EnumSet.allOf(Permissions::class.java)
        ) {

        }

        fun check(account: Account, permission: Permissions): Boolean {

        }
    }
}