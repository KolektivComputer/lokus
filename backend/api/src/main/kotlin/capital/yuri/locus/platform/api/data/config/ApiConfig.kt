package capital.yuri.locus.platform.api.data.config

import capital.yuri.locus.platform.core.config.Config
import capital.yuri.locus.platform.core.config.ConfigLocation
import capital.yuri.locus.platform.core.config.ConfigScope
import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Config(name = "api", scope = ConfigScope.Root, location = ConfigLocation.File)
@Serializable
data class ApiConfig(val baseUrl: Url = Url("http://127.0.0.1:8080"))
