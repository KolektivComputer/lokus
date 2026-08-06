package capital.yuri.locus.platform.core.config

import capital.yuri.locus.common.resource.ResourceLoader
import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.version.services.VersionService
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import java.nio.file.Path

val configModule = module {
    single { ResourceLoader() }
    single { ConfigService(get<Path>()) }
    single<VersionService>()
}
