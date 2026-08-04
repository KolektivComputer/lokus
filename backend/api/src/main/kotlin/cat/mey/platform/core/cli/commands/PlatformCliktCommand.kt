package cat.mey.platform.core.cli.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.path

open class PlatformCliktCommand(name: String = "locus") : SuspendingCliktCommand(name) {
    protected val configDirectory by argument().path(
        mustExist = true,
        canBeFile = false,
        mustBeReadable = true,
    )

    override suspend fun run() = Unit
}
