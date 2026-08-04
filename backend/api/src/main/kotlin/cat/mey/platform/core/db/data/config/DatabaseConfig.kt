package cat.mey.platform.core.db.data.config

import cat.mey.platform.core.config.ConfigFile
import cat.mey.platform.core.config.ConfigType
import cat.mey.platform.core.config.Environment
import cat.mey.platform.core.config.JavaProperty
import cat.mey.platform.core.db.data.DatabaseType
import kotlinx.serialization.Serializable

@ConfigFile(name = "database", type = ConfigType.Root)
@Serializable
data class DatabaseConfig(
    val jdbcUrl: String = "jdbc:postgresql://localhost:5432/thingy",
    var driver: DatabaseType = DatabaseType.PostgreSQL,

    @Environment("OVERLAY_DB_USER")
    @JavaProperty("overlay.db.user")
    var username: String? = null,

    @Environment("OVERLAY_DB_PASS")
    @JavaProperty("overlay.db.pass")
    var password: String? = null,
)
