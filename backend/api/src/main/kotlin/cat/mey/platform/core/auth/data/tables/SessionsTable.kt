@file:OptIn(ExperimentalUuidApi::class)

package cat.mey.platform.core.auth.data.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.uuid.ExperimentalUuidApi

object SessionsTable : UuidTable("sessions") {
    val account = reference("account", AccountsTable, onDelete = ReferenceOption.CASCADE)
    var name = varchar("name", 255).nullable()
    // todo: store client data
}