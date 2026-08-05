package capital.yuri.locus.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.net.HttpURLConnection
import java.net.URI

/**
 * Standalone CLI (`locus-cli`).
 *
 * [MigrateCommand] talks to a running api daemon over HTTP for now.
 * A later PR will prefer a Unix domain socket control plane.
 */
suspend fun main(args: Array<String>) = LocusCli()
    .subcommands(VersionCommand(), MigrateCommand())
    .main(args)

class LocusCli : SuspendingCliktCommand("locus-cli") {
    override suspend fun run() = Unit
}

class VersionCommand : SuspendingCliktCommand("version") {
    private val verbose by option("-v", "--verbose").flag()

    override suspend fun run() {
        echo("locus-cli")
        if (verbose) {
            echo("Control plane: HTTP (Unix domain socket planned)")
        }
    }
}

/**
 * Ask the daemon to run [DatabaseMigrationService.migrate] against the table registry.
 */
class MigrateCommand : SuspendingCliktCommand("migrate") {
    private val baseUrl by option("--url", help = "Daemon base URL")
        .default("http://127.0.0.1:8080")

    override suspend fun run() {
        val endpoint = baseUrl.trimEnd('/') + "/api/admin/migrate"
        echo("Requesting migration via $endpoint …")

        val conn = (URI(endpoint).toURL().openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 120_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            // empty body
            outputStream.use { it.write(ByteArray(0)) }
        }

        val code = conn.responseCode
        val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.readText()
            .orEmpty()

        if (code in 200..299) {
            echo("Migration OK ($code): $body")
        } else {
            echo("Migration failed ($code): $body", err = true)
            throw ProgramResult(1)
        }
    }
}

/** Clikt-friendly non-zero exit without a stack trace. */
class ProgramResult(val status: Int) : RuntimeException("exit $status")
