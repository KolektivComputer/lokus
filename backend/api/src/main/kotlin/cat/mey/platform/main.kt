package cat.mey.platform

import cat.mey.platform.api.cli.commands.ApiCliktCommand
import cat.mey.platform.core.cli.commands.MigrateCliktCommand
import cat.mey.platform.core.cli.commands.PlatformCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands

suspend fun main(args: Array<String>) = PlatformCliktCommand()
    .subcommands(
        MigrateCliktCommand(),
        ApiCliktCommand(),
    ).main(args)