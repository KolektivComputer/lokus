package capital.yuri.locus.platform.core.db.services

import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DatabaseService : KoinComponent {
    private val configService: ConfigService by inject()
    private val config by configService.config<DatabaseConfig>()

    fun connect() {
        Database.connect(
            url = config.jdbcUrl,
            driver = config.driver.driver,
            user = config.username ?: "",
            password = config.password ?: "",
        )
    }
}
