package capital.yuri.locus.platform

import capital.yuri.locus.platform.api.cli.commands.ApiCliktCommand
import capital.yuri.locus.platform.core.cli.commands.MigrateCliktCommand
import capital.yuri.locus.platform.core.cli.commands.LocusCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands

suspend fun main(args: Array<String>) = LocusCliktCommand()
    .subcommands(
        MigrateCliktCommand(),
        ApiCliktCommand(),
    ).main(args)
