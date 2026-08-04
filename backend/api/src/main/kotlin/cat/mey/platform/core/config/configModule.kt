package cat.mey.platform.core.config

import cat.mey.platform.api.data.config.ApiConfig
import cat.mey.platform.core.auth.data.config.AuthConfig
import cat.mey.platform.core.cli.CommandLine
import cat.mey.platform.core.config.services.ConfigService
import cat.mey.platform.core.db.data.config.DatabaseConfig
import org.koin.dsl.module

val configModule = module {
    single<ConfigService> { ConfigService(get<CommandLine>().configDirectory) }

    single<AuthConfig> { get<ConfigService>().auth }
    single<DatabaseConfig> { get<ConfigService>().database }
    single<ApiConfig> { get<ConfigService>().api }
}