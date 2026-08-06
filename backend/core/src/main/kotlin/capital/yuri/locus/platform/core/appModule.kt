package capital.yuri.locus.platform.core

import capital.yuri.locus.platform.core.config.configModule
import org.koin.dsl.module
import java.nio.file.Path

/**
 * Application Koin graph.
 *
 * Only runtime input is the config directory (from the CLI). Everything else is
 * pure feature modules included via [coreModule] / [configModule].
 */
fun appModule(configDirectory: Path) = module {
    single<Path> { configDirectory }
    includes(configModule, coreModule)
}
