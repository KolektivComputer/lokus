package capital.yuri.locus.platform.core.extension

import capital.yuri.locus.platform.core.cli.CommandLine
import capital.yuri.locus.platform.core.coreModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun createExtensionModule(commandLine: CommandLine) = module {
    includes(coreModule)

    scope(named("extension")) {
    }
}
