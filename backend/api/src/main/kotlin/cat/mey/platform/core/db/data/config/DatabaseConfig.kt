package cat.mey.platform.core.db.data.config

import cat.mey.core.config.Environment
import cat.mey.core.config.JavaProperty
import cat.mey.core.db.data.DatabaseType
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val jdbcUrl: String,
    var driver: DatabaseType,

    @Environment("OVERLAY_DB_USER")
    @JavaProperty("overlay.db.user")
    var username: String? = null,

    @Environment("OVERLAY_DB_PASS")
    @JavaProperty("overlay.db.pass")
    var password: String? = null,
)