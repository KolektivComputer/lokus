package capital.yuri.locus.platform.core.auth

import capital.yuri.locus.platform.core.auth.services.AccountAuthenticationService
import capital.yuri.locus.platform.core.auth.services.Argon2Service
import capital.yuri.locus.platform.core.auth.services.JwtService
import capital.yuri.locus.platform.core.auth.services.SessionService
import org.koin.dsl.module

val authModule = module {
    single { Argon2Service() }
    single { AccountAuthenticationService() }
    single { JwtService() }
    single { SessionService() }
}
