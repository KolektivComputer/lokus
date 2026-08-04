package capital.yuri.locus.platform.core.domain.services.repositories

import capital.yuri.locus.platform.core.domain.data.entities.Domain
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.core.component.KoinComponent

class DomainRepository : KoinComponent {
    suspend fun findByFqdnSuspend(fqdn: String) = suspendTransaction {
        Domain.findByFqdn(fqdn)
    }
}
