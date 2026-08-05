package capital.yuri.locus.platform.api.cli.commands

import capital.yuri.locus.platform.api.auth.configureAuth
import capital.yuri.locus.platform.api.data.config.ApiConfig
import capital.yuri.locus.platform.api.routes.apiRoutes
import capital.yuri.locus.platform.core.appModule
import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import capital.yuri.locus.platform.core.db.services.DatabaseService
import capital.yuri.locus.platform.core.scheduling.services.SchedulerService
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.host
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.nio.file.Path

class ApiCliktCommand : SuspendingCliktCommand("api") {
    val configDirectory by requireObject<Path>()

    val host: String by option(help = "Host name").default("127.0.0.1")
    val port: Int by option(help = "").int().default(8080)

    override suspend fun run() {
        embeddedServer(Netty, port = port, host = host) {
            install(Koin) {
                slf4jLogger()
                modules(appModule(configDirectory))
            }

            install(WebSockets.Plugin)

            get<ConfigService>().loadConfigsStartup(
                DatabaseConfig::class,
                AuthConfig::class,
                ApiConfig::class,
            )
            get<DatabaseService>().connect()
            // JDBC job store needs the shared Hikari pool
            get<SchedulerService>().start()

            configureAuth()

            val json = Json {
                ignoreUnknownKeys = true
                classDiscriminator = "type"
            }

            install(ContentNegotiation) {
                json(json)
            }

            routing {

                val configService by inject<ConfigService>()
                val apiConfig by configService.config<ApiConfig>()
                host(apiConfig.baseUrl.host) {
                    apiRoutes()
                }
            }
        }.start(wait = true)
    }
}
