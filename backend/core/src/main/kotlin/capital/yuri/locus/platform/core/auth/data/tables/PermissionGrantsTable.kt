@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.core.auth.data.tables

import capital.yuri.locus.platform.core.auth.data.types.permission.Permissions
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import kotlin.uuid.ExperimentalUuidApi

object PermissionGrantsTable : CompositeIdTable("permission_grants") {
    val account = reference("account", AccountsTable)
    val permission = enumeration<Permissions>("permission")

    override val primaryKey = PrimaryKey(account, permission)
}
