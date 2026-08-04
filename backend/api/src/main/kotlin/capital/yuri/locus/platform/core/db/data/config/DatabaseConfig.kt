package capital.yuri.locus.platform.core.db.data.config

import capital.yuri.locus.platform.core.config.ConfigFile
import capital.yuri.locus.platform.core.config.ConfigType
import capital.yuri.locus.platform.core.config.Environment
import capital.yuri.locus.platform.core.config.JavaProperty
import capital.yuri.locus.platform.core.db.data.DatabaseType
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
