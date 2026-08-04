package cat.mey.platform.core.config

import cat.mey.platform.api.data.config.ApiConfig
import cat.mey.platform.core.auth.data.config.AuthConfig
import cat.mey.platform.core.cli.CommandLine
import cat.mey.platform.core.config.services.ConfigService
import cat.mey.platform.core.db.data.config.DatabaseConfig
import org.koin.dsl.module

val configModule = module {
    single<ConfigService> {
        ConfigService(get<CommandLine>().configDirectory)
    }

    // factory so each resolve can pick up a hot-reloaded value from ConfigService
    factory<DatabaseConfig> { get<ConfigService>().config() }
    factory<AuthConfig> { get<ConfigService>().config() }
    factory<ApiConfig> { get<ConfigService>().config() }
}
