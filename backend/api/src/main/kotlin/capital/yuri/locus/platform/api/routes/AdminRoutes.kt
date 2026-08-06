package capital.yuri.locus.platform.api.routes

import capital.yuri.locus.platform.core.db.services.DatabaseMigrationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

/**
 * Internal admin endpoints. Auth / host lockdown is a follow-up;
 * intended to be invoked by locus-cli against a local daemon.
 */
fun Route.adminRoutes() {
    val migrationService by inject<DatabaseMigrationService>()
    val logger = LoggerFactory.getLogger("AdminRoutes")

    route("/admin") {
        post("/migrate") {
            try {
                migrationService.migrate()
                call.respond(HttpStatusCode.OK, MigrateResponse.Ok)
            } catch (e: Exception) {
                logger.error("Migration failed", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    MigrateResponse.Error(e.message ?: "migration failed"),
                )
            }
        }
    }
}

@Serializable
sealed interface MigrateResponse {
    @Serializable
    @SerialName("ok")
    data object Ok : MigrateResponse

    @Serializable
    @SerialName("error")
    data class Error(val message: String) : MigrateResponse
}
