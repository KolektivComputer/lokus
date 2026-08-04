package capital.yuri.locus.platform.api.data.config

import capital.yuri.locus.platform.core.config.ConfigFile
import capital.yuri.locus.platform.core.config.ConfigType
import io.ktor.http.Url
import kotlinx.serialization.Serializable

@ConfigFile(name = "api", type = ConfigType.Root)
@Serializable
data class ApiConfig(val baseUrl: Url = Url("http://127.0.0.1:8080"))
