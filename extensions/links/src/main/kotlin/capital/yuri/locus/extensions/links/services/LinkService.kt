package capital.yuri.locus.extensions.links.services

import capital.yuri.locus.extensions.links.data.entities.Link
import capital.yuri.locus.platform.core.auth.data.entities.Account
import io.ktor.http.Url
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import java.security.SecureRandom

class LinkService : KoinComponent {
    private val random = SecureRandom()

    private fun generateRawShortId(): String {
        val sb = StringBuilder(ID_LENGTH)
        repeat(ID_LENGTH) {
            sb.append(CHARSET[random.nextInt(CHARSET.length)])
        }
        return sb.toString()
    }

    fun generateShortId(exists: (String) -> Boolean, maxAttempts: Int = 32): String {
        repeat(maxAttempts) {
            val candidate = generateRawShortId()
            if (!exists(candidate)) return candidate
        }
        error("Failed to generate a unique share id after $maxAttempts attempts")
    }

    suspend fun generateShortIdSuspend(exists: suspend (String) -> Boolean, maxAttempts: Int = 32): String {
        repeat(maxAttempts) {
            val candidate = generateRawShortId()
            if (!exists(candidate)) return candidate
        }
        error("Failed to generate a unique short id after $maxAttempts attempts")
    }

    fun findLinkByVanityOrShortId(account: Account, searchTerm: String) = transaction {
        Link.findByVanityOrShortId(account, searchTerm)
    }

    suspend fun findLinkByVanityOrShortIdSuspend(account: Account, searchTerm: String) = suspendTransaction {
        Link.findByVanityOrShortId(account, searchTerm)
    }

    fun linkExistsByVanityOrShortId(account: Account, searchTerm: String) = transaction {
        Link.findByVanityOrShortId(account, searchTerm) != null
    }

    suspend fun linkExistsByVanityOrShortIdSuspend(account: Account, searchTerm: String) = suspendTransaction {
        Link.findByVanityOrShortId(account, searchTerm) != null
    }

    fun findLinkByVanity(account: Account, vanity: String) = transaction {
        Link.findByVanity(account, vanity)
    }

    suspend fun findLinkByVanitySuspend(account: Account, vanity: String) = suspendTransaction {
        Link.findByVanity(account, vanity)
    }

    fun linkExistsByVanity(account: Account, vanity: String) = transaction {
        Link.findByVanity(account, vanity) != null
    }

    suspend fun linkExistsByVanitySuspend(account: Account, vanity: String) = suspendTransaction {
        Link.findByVanity(account, vanity) != null
    }

    fun findLinkByShortId(account: Account, shortId: String) = transaction {
        Link.findByShortId(account, shortId)
    }

    suspend fun findLinkByShortIdSuspend(account: Account, shortId: String) = suspendTransaction {
        Link.findByShortId(account, shortId)
    }

    fun linkExistsByShortId(account: Account, shortId: String) = transaction {
        Link.findByShortId(account, shortId) != null
    }

    suspend fun linkExistsByShortIdSuspend(account: Account, shortId: String) = suspendTransaction {
        Link.findByShortId(account, shortId) != null
    }

    fun createLink(account: Account, destination: Url) = transaction {
        val shortId = generateShortId(
            exists = { candidate ->
                linkExistsByShortId(account, candidate)
            },
        )

        Link.create(account, destination, shortId)
    }

    companion object {
        private const val ID_LENGTH = 11
        private const val CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    }
}
