package cat.mey.platform.api.data.config

import cat.mey.platform.core.config.ConfigFile
import cat.mey.platform.core.config.ConfigType
import io.ktor.http.Url
import kotlinx.serialization.Serializable

@ConfigFile(name = "api", type = ConfigType.Root)
@Serializable
data class ApiConfig(
    val baseUrl: Url = Url("http://127.0.0.1:8080"),
)
