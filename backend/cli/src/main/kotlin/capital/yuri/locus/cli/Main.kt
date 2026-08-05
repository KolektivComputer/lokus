package capital.yuri.locus.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

/**
 * Standalone CLI binary (`locus-cli`).
 *
 * Today: local helpers only. A later PR will talk to the api daemon over a
 * Unix domain socket (preferred for Docker/Alpine) rather than D-Bus.
 */
suspend fun main(args: Array<String>) = LocusCli()
    .subcommands(VersionCommand())
    .main(args)

class LocusCli : SuspendingCliktCommand("locus-cli") {
    override suspend fun run() = Unit
}

class VersionCommand : SuspendingCliktCommand("version") {
    private val verbose by option("-v", "--verbose").flag()

    override suspend fun run() {
        // Daemon IPC will supply live VersionService data later.
        echo("locus-cli (daemon IPC not yet implemented)")
        if (verbose) {
            echo("Future transport: Unix domain socket (Docker/Alpine friendly)")
        }
    }
}
