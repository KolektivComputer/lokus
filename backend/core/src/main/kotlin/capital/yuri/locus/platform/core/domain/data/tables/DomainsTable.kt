@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.core.domain.data.tables

import capital.yuri.locus.platform.core.auth.data.tables.AccountsTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.uuid.ExperimentalUuidApi

object DomainsTable : UuidTable("domains") {
    val account = reference("account", AccountsTable, onDelete = ReferenceOption.CASCADE)
    val fqdn = varchar("fqdn", 64).uniqueIndex()
    val verifiedAt = timestamp("verified_at").nullable().default(null)
    val verificationToken = varchar("verificationToken", 64)
    val endpoint = varchar("endpoint", 128).nullable().default(null)
}
