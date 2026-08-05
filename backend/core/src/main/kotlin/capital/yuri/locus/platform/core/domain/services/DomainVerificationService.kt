package capital.yuri.locus.platform.core.domain.services

import capital.yuri.locus.platform.core.domain.data.entities.Domain
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.time.Clock

class DomainVerificationService : KoinComponent {
    private val secureRandom = SecureRandom()

    fun verifyDomain(domain: Domain, providedToken: String): Boolean {
        val match = domain.verificationToken == providedToken

        if (match) {
            transaction {
                domain.verifiedAt = Clock.System.now()
            }
        }

        return match
    }

    fun verifyDomainSuspend(domain: Domain, providedToken: String) {
    }

    fun generateVerificationToken(): String {
        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        // Encode to a URL-safe format to prevent DNS parsing issues
        val token = Base64.encode(randomBytes)
            .replace("+", "-")
            .replace("/", "_")
            .replace("=", "")
        return token
    }
}
