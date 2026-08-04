package cat.mey.platform.api.data.config

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class ApiConfig(
    val baseUrl: Url,
)