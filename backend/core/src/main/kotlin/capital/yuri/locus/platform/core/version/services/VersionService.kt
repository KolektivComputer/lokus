package capital.yuri.locus.platform.core.version.services

import capital.yuri.locus.common.resource.ResourceLoadResult
import capital.yuri.locus.common.resource.ResourceLoader
import capital.yuri.locus.platform.core.version.data.types.ComponentKind
import capital.yuri.locus.platform.core.version.data.types.ComponentVersion
import capital.yuri.locus.platform.core.version.data.types.CoreVersionInfo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks core, extension, and web-bundle versions.
 * Core info is loaded from classpath `/version.json` via [ResourceLoader].
 */
class VersionService : KoinComponent {
    private val logger = LoggerFactory.getLogger(VersionService::class.java)
    private val resourceLoader by inject<ResourceLoader>()

    private val components = ConcurrentHashMap<String, ComponentVersion>()

    val core: CoreVersionInfo by lazy { loadCore() }

    fun registerExtension(id: String, version: String) {
        components[id] = ComponentVersion(id, version, ComponentKind.Extension)
    }

    fun registerWebBundle(id: String, version: String) {
        components[id] = ComponentVersion(id, version, ComponentKind.WebBundle)
    }

    fun unregister(id: String) {
        components.remove(id)
    }

    fun listComponents(): List<ComponentVersion> =
        buildList {
            add(
                ComponentVersion(
                    id = "core",
                    version = core.tag ?: core.commit ?: "unknown",
                    kind = ComponentKind.Core,
                ),
            )
            addAll(components.values.sortedBy { it.id })
        }

    private fun loadCore(): CoreVersionInfo {
        return when (val result = resourceLoader.load<CoreVersionInfo>(VERSION_RESOURCE)) {
            is ResourceLoadResult.Ok -> result.value.normalizedForDev()
            is ResourceLoadResult.NotFound -> {
                logger.debug("No {} on classpath", VERSION_RESOURCE)
                CoreVersionInfo()
            }
            is ResourceLoadResult.DecodeError -> {
                logger.warn("Failed to decode {}: {}", result.path, result.message)
                CoreVersionInfo()
            }
        }
    }

    /** Dev builds (dirty working tree) never advertise an update URL. */
    private fun CoreVersionInfo.normalizedForDev(): CoreVersionInfo =
        if (dirty) copy(updateUrl = null) else this

    companion object {
        const val VERSION_RESOURCE = "/version.json"
    }
}
