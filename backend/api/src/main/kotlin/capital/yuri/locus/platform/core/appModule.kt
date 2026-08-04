package capital.yuri.locus.platform.core

import capital.yuri.locus.platform.api.data.config.ApiConfig
import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import org.koin.dsl.module
import java.nio.file.Path

/**
 * Application Koin graph.
 *
 * Only runtime input is the config directory (from the CLI). Everything else is
 * pure feature modules included via [coreModule].
 */
fun appModule(configDirectory: Path) = module {
    single { ConfigService(configDirectory) }

    factory { get<ConfigService>().config<DatabaseConfig>().value }
    factory { get<ConfigService>().config<AuthConfig>().value }
    factory { get<ConfigService>().config<ApiConfig>().value }

    includes(coreModule)
}
