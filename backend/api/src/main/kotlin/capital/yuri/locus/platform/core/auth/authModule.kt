package capital.yuri.locus.platform.core.auth

import capital.yuri.locus.platform.core.auth.services.AccountAuthenticationService
import capital.yuri.locus.platform.core.auth.services.Argon2Service
import capital.yuri.locus.platform.core.auth.services.JwtService
import capital.yuri.locus.platform.core.auth.services.SessionService
import capital.yuri.locus.platform.core.config.configModule
import capital.yuri.locus.platform.core.db.dbModule
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val authModule = module {
    includes(dbModule, configModule)

    single<Argon2Service>()
    single<AccountAuthenticationService>()
    single<JwtService>()
    single<SessionService>()
}
