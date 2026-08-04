package capital.yuri.locus.platform.api

import capital.yuri.locus.platform.core.auth.authModule
import capital.yuri.locus.platform.core.cli.CommandLine
import capital.yuri.locus.platform.core.config.createConfigModule
import capital.yuri.locus.platform.core.coreModule
import org.koin.dsl.module

fun createApiModule(commandLine: CommandLine) = module {
    includes(
        createConfigModule(commandLine),
        coreModule,
        authModule,
    )
}
