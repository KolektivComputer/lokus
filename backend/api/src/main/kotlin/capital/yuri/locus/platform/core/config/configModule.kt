package capital.yuri.locus.platform.core.config

import capital.yuri.locus.platform.core.cli.CommandLine
import capital.yuri.locus.platform.core.config.services.ConfigService
import org.koin.dsl.module

val configModule = module {
    single<ConfigService> {
        ConfigService(get<CommandLine>().configDirectory)
    }
}
