package cat.mey.platform.core.extension

import cat.mey.platform.core.cli.CommandLine
import cat.mey.platform.core.coreModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun createExtensionModule(commandLine: CommandLine) = module {
    includes(coreModule)

    scope(named("extension")) {

    }
}