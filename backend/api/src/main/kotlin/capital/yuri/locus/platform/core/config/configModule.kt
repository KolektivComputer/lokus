package capital.yuri.locus.platform.core.config

import capital.yuri.locus.platform.api.data.config.ApiConfig
import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import capital.yuri.locus.platform.core.cli.CommandLine
import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import org.koin.dsl.module

val configModule = module {
    single<ConfigService> {
        ConfigService(get<CommandLine>().configDirectory)
    }

    // Resolve through the node so hot-reloads are visible
    factory<DatabaseConfig> { get<ConfigService>().config<DatabaseConfig>().value }
    factory<AuthConfig> { get<ConfigService>().config<AuthConfig>().value }
    factory<ApiConfig> { get<ConfigService>().config<ApiConfig>().value }
}
