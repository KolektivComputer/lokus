package cat.mey.platform.core.domain

import cat.mey.platform.core.cli.CommandLine
import cat.mey.platform.core.coreModule
import cat.mey.platform.core.db.dbModule
import cat.mey.platform.core.domain.services.DomainVerificationService
import cat.mey.platform.core.domain.services.repositories.DomainRepository
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