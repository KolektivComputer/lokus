package capital.yuri.locus.platform.core.config

import capital.yuri.locus.platform.api.data.config.ApiConfig
import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import capital.yuri.locus.platform.core.resource.services.ResourceLoader
import capital.yuri.locus.platform.core.version.services.VersionService
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import java.nio.file.Path

val configModule = module {
    single { ResourceLoader() }
    single { ConfigService(get<Path>()) }
    single<VersionService>()

    factory { get<ConfigService>().config<DatabaseConfig>().value }
    factory { get<ConfigService>().config<AuthConfig>().value }
    factory { get<ConfigService>().config<ApiConfig>().value }
}
