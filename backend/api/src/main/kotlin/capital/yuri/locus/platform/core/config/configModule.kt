package capital.yuri.locus.platform.core.config

import capital.yuri.locus.platform.api.data.config.ApiConfig
import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import capital.yuri.locus.platform.core.cli.CommandLine
import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import org.koin.dsl.module
import java.nio.file.Path

/**
 * Config factories only. [ConfigService] itself is registered in
 * [createConfigModule] so it does not depend on a graph-level [CommandLine].
 */
val configModule = module {
    factory<DatabaseConfig> { get<ConfigService>().config<DatabaseConfig>().value }
    factory<AuthConfig> { get<ConfigService>().config<AuthConfig>().value }
    factory<ApiConfig> { get<ConfigService>().config<ApiConfig>().value }
}

/** Entry-point module: registers [CommandLine] + [ConfigService] with a concrete path. */
fun createConfigModule(commandLine: CommandLine) = module {
    single<CommandLine> { commandLine }
    single<ConfigService> { ConfigService(commandLine.configDirectory) }
    includes(configModule)
}

/** Same as above when you only have a [Path]. */
fun createConfigModule(configDirectory: Path) =
    createConfigModule(CommandLine(configDirectory))
