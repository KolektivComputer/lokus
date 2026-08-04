package cat.mey.platform.core.auth.data.tables

import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object AccountsTable : UuidTable("accounts") {
    val username = varchar("username", 255).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 128)
}