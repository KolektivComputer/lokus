package capital.yuri.locus.platform.core.domain

import capital.yuri.locus.platform.core.cli.CommandLine
import capital.yuri.locus.platform.core.coreModule
import capital.yuri.locus.platform.core.db.dbModule
import capital.yuri.locus.platform.core.domain.services.DomainVerificationService
import capital.yuri.locus.platform.core.domain.services.repositories.DomainRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

fun createDomainModule(commandLine: CommandLine) = module {
    includes(dbModule, coreModule)

    single<DomainRepository>()
    single<DomainVerificationService>()

    scope(named("domain")) {
    }
}
