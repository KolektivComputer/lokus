package capital.yuri.locus.platform.api

import capital.yuri.locus.platform.core.appModule
import capital.yuri.locus.platform.core.cli.CommandLine
import org.koin.core.module.Module
import java.nio.file.Path

/** @deprecated Prefer [appModule] directly. */
fun createApiModule(commandLine: CommandLine): Module =
    appModule(commandLine.configDirectory)

/** @deprecated Prefer [appModule] directly. */
fun createApiModule(configDirectory: Path): Module =
    appModule(configDirectory)
