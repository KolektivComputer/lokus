package capital.yuri.locus.platform.core.config

import capital.yuri.locus.platform.api.data.config.ApiConfig
import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import org.koin.dsl.module
import java.nio.file.Path

/**
 * Top-level so the Koin compiler plugin can see [ConfigService] at call sites.
 * [Path] is supplied by [capital.yuri.locus.platform.core.appModule].
 */
val configModule = module {
    single { ConfigService(get<Path>()) }

    factory { get<ConfigService>().config<DatabaseConfig>().value }
    factory { get<ConfigService>().config<AuthConfig>().value }
    factory { get<ConfigService>().config<ApiConfig>().value }
}
