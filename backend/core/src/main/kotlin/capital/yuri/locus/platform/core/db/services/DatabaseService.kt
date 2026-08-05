package capital.yuri.locus.platform.core.db.services

import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import javax.sql.DataSource

/**
 * Owns the process-wide JDBC pool (HikariCP). Exposed and Quartz both use [dataSource].
 */
class DatabaseService : KoinComponent {
    private val logger = LoggerFactory.getLogger(DatabaseService::class.java)
    private val configService by inject<ConfigService>()
    private val config by configService.config<DatabaseConfig>()

    private var _dataSource: HikariDataSource? = null

    /** Shared pool — available after [connect]. */
    val dataSource: DataSource
        get() = _dataSource
            ?: error("DatabaseService.connect() has not been called")

    val isConnected: Boolean
        get() = _dataSource?.isClosed == false

    fun connect() {
        if (_dataSource != null && !_dataSource!!.isClosed) {
            logger.debug("Database already connected")
            return
        }

        val hikari = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            driverClassName = config.driver.driver
            username = config.username ?: ""
            password = config.password ?: ""
            poolName = "locus"
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 10_000
            validationTimeout = 5_000
            isAutoCommit = false
        }

        val ds = HikariDataSource(hikari)
        // Fail fast if credentials / network are wrong
        ds.connection.use { conn ->
            conn.createStatement().use { st -> st.execute("SELECT 1") }
        }

        Database.connect(ds)
        _dataSource = ds
        logger.info("HikariCP connected ({})", config.jdbcUrl)
    }

    fun close() {
        _dataSource?.close()
        _dataSource = null
    }
}
