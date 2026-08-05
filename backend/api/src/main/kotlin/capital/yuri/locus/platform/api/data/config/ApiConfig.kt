package capital.yuri.locus.platform.api.data.config

import capital.yuri.locus.platform.core.config.Config
import capital.yuri.locus.platform.core.config.ConfigLocation
import capital.yuri.locus.platform.core.config.ConfigScope
import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Config(name = "api", scope = ConfigScope.Root, location = ConfigLocation.File)
@Serializable
data class ApiConfig(
    /** Canonical public URL of this API (used for links, JWT audience, etc.). */
    val baseUrl: Url = Url("http://127.0.0.1:8080"),

    /**
     * Hostnames accepted for the main API ([Host] header).
     * [baseUrl] host is always included even if omitted here.
     */
    val hosts: List<String> = listOf("127.0.0.1", "localhost"),

    /**
     * Hostnames for browser / frontend plugs (themes, bundles).
     * Wired when frontend routing lands; listed here so one config owns both.
     */
    val frontendHosts: List<String> = listOf("127.0.0.1", "localhost"),
) {
    /** Distinct hostnames for Ktor `host(List)` on the main API. */
    fun apiHostNames(): List<String> =
        (hosts + baseUrl.host)
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()

    fun frontendHostNames(): List<String> =
        frontendHosts
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
}
