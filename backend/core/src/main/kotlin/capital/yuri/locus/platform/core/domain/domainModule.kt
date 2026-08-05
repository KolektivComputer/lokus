package capital.yuri.locus.platform.core.domain

import capital.yuri.locus.platform.core.domain.services.DomainVerificationService
import capital.yuri.locus.platform.core.domain.services.EndpointRegistryService
import capital.yuri.locus.platform.core.domain.services.repositories.DomainRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val domainModule = module {
    single { DomainRepository() }
    single { DomainVerificationService() }
    single { EndpointRegistryService() }

    scope(named("domain")) {
    }
}
