package cat.mey.platform.core.db.services

import cat.mey.platform.core.db.data.config.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DatabaseService : KoinComponent {
    private val config by inject<DatabaseConfig>()

    fun connect() {
        Database.connect(
            url = config.jdbcUrl,
            driver = config.driver.driver,
            user = config.username ?: "",
            password = config.password ?: "",
        )
    }
}
