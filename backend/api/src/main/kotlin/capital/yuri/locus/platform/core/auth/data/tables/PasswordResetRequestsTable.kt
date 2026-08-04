@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.core.auth.data.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

object PasswordResetRequestsTable : UuidTable("password_reset_requests") {
    val account = reference("account", AccountsTable, ReferenceOption.CASCADE)
    val token = varchar("token", 255).uniqueIndex()
    val expired = timestamp("expired").nullable()
    val created = timestamp("created").default(Clock.System.now())
}
