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
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

/**
 * Discovers [ExtensionProvider]s via ServiceLoader:
 * 1. Application classpath (rarely used — prefer JARs)
 * 2. `*.jar` under [extensionsDirectory]
 * 3. `*.jar` under [extensionsDirectory]/drop-in` (host-mounted extras)
 *
 * Each JAR gets its own [URLClassLoader] parented on the app ClassLoader so
 * core types resolve from the host while extension classes stay isolated.
 */
class ExtensionLoaderService : KoinComponent {
    private val logger = LoggerFactory.getLogger(ExtensionLoaderService::class.java)

    private val loaded = ConcurrentHashMap<ExtensionId, Extension>()
    private val jarLoaders = mutableListOf<URLClassLoader>()

    fun loaded(): Collection<Extension> = loaded.values

    fun get(id: ExtensionId): Extension? = loaded[id]

    /**
     * @param extensionsDirectory folder of extension JARs (e.g. `/app/extensions`)
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
                    logger.info("Discovered extension provider {} from {}", key, source)
                }
            }
        }

        // Optional classpath providers (tests / special embeddings)
        absorb(ExtensionProvider::class.java.classLoader, "classpath")

        if (extensionsDirectory != null) {
            loadJarsFrom(extensionsDirectory, providers, ::absorb)
            val dropIn = extensionsDirectory.resolve("drop-in")
            if (dropIn.isDirectory()) {
                loadJarsFrom(dropIn, providers, ::absorb)
            }
        }

        return providers.values.toList()
    }

    private fun loadJarsFrom(
        directory: Path,
        providers: MutableMap<String, ExtensionProvider>,
        absorb: (ClassLoader, String) -> Unit,
    ) {
        if (!directory.isDirectory()) {
            logger.debug("Extensions directory missing: {}", directory)
            return
        }

        val jars = Files.list(directory).use { stream ->
            stream.asSequence()
                .filter { it.isRegularFile() && it.extension.equals("jar", ignoreCase = true) }
                .toList()
        }

        if (jars.isEmpty()) {
            logger.debug("No extension jars in {}", directory)
            return
        }

        logger.info("Scanning {} extension jar(s) in {}", jars.size, directory)

        for (jar in jars) {
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
