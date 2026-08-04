package cat.mey.platform.core.auth

import cat.mey.platform.core.auth.services.AccountAuthenticationService
import cat.mey.platform.core.auth.services.Argon2Service
import cat.mey.platform.core.auth.services.JwtService
import cat.mey.platform.core.auth.services.SessionService
import cat.mey.platform.core.config.configModule
import cat.mey.platform.core.db.dbModule
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val authModule = module {
    includes(dbModule, configModule)

    single<Argon2Service>()
    single<AccountAuthenticationService>()
    single<JwtService>()
    single<SessionService>()
}