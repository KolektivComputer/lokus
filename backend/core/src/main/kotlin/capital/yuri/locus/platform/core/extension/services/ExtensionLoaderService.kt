package capital.yuri.locus.platform.core.extension.services

import capital.yuri.locus.platform.core.db.services.TableRegistryService
import capital.yuri.locus.platform.core.domain.services.EndpointRegistryService
import capital.yuri.locus.platform.core.extension.ExtensionProvider
import capital.yuri.locus.platform.core.extension.data.types.Extension
import capital.yuri.locus.platform.core.extension.data.types.ExtensionId
import capital.yuri.locus.platform.core.version.services.VersionService
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.slf4j.LoggerFactory
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile

/**
 * Discovers [ExtensionProvider]s via ServiceLoader on the app classpath and
 * any `*.jar` under [extensionsDirectory], loads their Koin modules, then
 * instantiates and installs each [Extension].
 */
class ExtensionLoaderService : KoinComponent {
    private val logger = LoggerFactory.getLogger(ExtensionLoaderService::class.java)

    private val loaded = ConcurrentHashMap<ExtensionId, Extension>()
    private val jarLoaders = mutableListOf<URLClassLoader>()

    fun loaded(): Collection<Extension> = loaded.values

    fun get(id: ExtensionId): Extension? = loaded[id]

    /**
     * @param extensionsDirectory optional folder of extension JARs (e.g. `/app/extensions`)
     */
    fun load(extensionsDirectory: Path? = null): List<Extension> {
        val providers = discover(extensionsDirectory)
        if (providers.isEmpty()) {
            logger.info("No extension providers discovered")
            return emptyList()
        }

        val modules = providers.flatMap { it.modules() }
        if (modules.isNotEmpty()) {
            loadKoinModules(modules)
            logger.info("Loaded {} Koin module(s) from {} provider(s)", modules.size, providers.size)
        }

        val endpointRegistry = get<EndpointRegistryService>()
        val tableRegistry = get<TableRegistryService>()
        val versionService = get<VersionService>()

        return providers.mapNotNull { provider ->
            if (loaded.containsKey(provider.id)) {
                logger.warn("Skipping duplicate extension id={}", provider.id.value)
                return@mapNotNull null
            }
            try {
                val extension = provider.create()
                extension.install(tableRegistry, endpointRegistry)
                versionService.registerExtension(provider.id.value, provider.version)
                loaded[provider.id] = extension
                logger.info("Installed extension {} ({}) v{}", provider.name, provider.id.value, provider.version)
                extension
            } catch (e: Exception) {
                logger.error("Failed to install extension {}", provider.id.value, e)
                null
            }
        }
    }

    fun discover(extensionsDirectory: Path?): List<ExtensionProvider> {
        val providers = linkedMapOf<String, ExtensionProvider>()

        fun absorb(classLoader: ClassLoader, source: String) {
            ServiceLoader.load(ExtensionProvider::class.java, classLoader).forEach { provider ->
                val key = provider.id.value
                if (providers.containsKey(key)) {
                    logger.debug("Provider {} already registered; skipping from {}", key, source)
                } else {
                    providers[key] = provider
                    logger.debug("Discovered extension provider {} from {}", key, source)
                }
            }
        }

        // Classpath modules (e.g. :extensions:links on the daemon classpath in monorepo)
        absorb(ExtensionProvider::class.java.classLoader, "classpath")

        if (extensionsDirectory != null && Files.isDirectory(extensionsDirectory)) {
            Files.list(extensionsDirectory).use { stream ->
                stream
                    .filter { it.isRegularFile() && it.extension.equals("jar", ignoreCase = true) }
                    .forEach { jar ->
                        try {
                            val loader = URLClassLoader(
                                arrayOf(jar.toUri().toURL()),
                                ExtensionProvider::class.java.classLoader,
                            )
                            jarLoaders += loader
                            absorb(loader, jar.fileName.toString())
                        } catch (e: Exception) {
                            logger.error("Failed to open extension jar {}", jar, e)
                        }
                    }
            }
        }

        return providers.values.toList()
    }
}
