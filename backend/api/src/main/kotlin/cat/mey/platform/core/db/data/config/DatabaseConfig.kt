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
    val jdbcUrl: String = "jdbc:postgresql://localhost:5432/locus",
    var driver: DatabaseType = DatabaseType.PostgreSQL,

    @Environment("LOCUS_DB_USER")
    @JavaProperty("locus.db.user")
    var username: String? = null,

    @Environment("LOCUS_DB_PASS")
    @JavaProperty("locus.db.pass")
    var password: String? = null,
)
