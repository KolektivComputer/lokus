@file:OptIn(ExperimentalUuidApi::class)

package cat.mey.platform.core.auth.data.tables

import cat.mey.platform.core.auth.data.types.permission.Permissions
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.uuid.ExperimentalUuidApi

object PermissionGrantsTable: CompositeIdTable("permission_grants") {
    val account = reference("account", AccountsTable)
    val permission = enumeration<Permissions>("permission").entityId()

    override val primaryKey = PrimaryKey(account, permission)


}