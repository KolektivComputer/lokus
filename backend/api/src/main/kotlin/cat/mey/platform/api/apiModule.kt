package cat.mey.platform.api

import cat.mey.platform.core.cli.CommandLine
import cat.mey.platform.core.coreModule
import org.koin.dsl.module

fun createApiModule(commandLine: CommandLine) = module {
    single { commandLine }
    includes(coreModule)
}