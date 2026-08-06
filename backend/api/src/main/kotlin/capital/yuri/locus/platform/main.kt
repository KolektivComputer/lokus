package capital.yuri.locus.platform

import capital.yuri.locus.platform.api.cli.commands.ApiCliktCommand
import capital.yuri.locus.platform.core.cli.commands.LocusCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands

/** Daemon entrypoint. Schema migrate lives on locus-cli (talks to this process). */
suspend fun main(args: Array<String>) = LocusCliktCommand()
    .subcommands(
        ApiCliktCommand(),
    ).main(args)
