package cat.mey.platform.core.config.services

import cat.mey.platform.core.config.ConfigFile
import cat.mey.platform.core.config.ConfigType
import cat.mey.platform.core.config.Environment
import cat.mey.platform.core.config.JavaProperty
import cat.mey.platform.core.config.data.types.results.ConfigLoadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.div
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Annotation-driven config loader with optional file watching.
 *
 * Usage:
 * ```
 * val db = configService.config<DatabaseConfig>()
 * val shop = configService.config<ShopConfig>(extensionId = "shop")
 * ```
 *
 * Paths:
 * - Root:      CONFIG_ROOT/<name>.jsonc
 * - Extension: CONFIG_ROOT/ext/<extensionId>/<name>.jsonc
 */
class ConfigService(
    private val directory: Path,
) : KoinComponent {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        allowComments = true
    }

    private val logger = LoggerFactory.getLogger(ConfigService::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private data class ConfigKey(
        val kClass: KClass<*>,
        val extensionId: String?,
    )

    private class ConfigNode<T : Any>(
        @Volatile var value: T,
        val file: File,
        val kClass: KClass<T>,
    )

    private val nodes = ConcurrentHashMap<ConfigKey, ConfigNode<*>>()
    private val watchService = FileSystems.getDefault().newWatchService()
    private val watchedDirs = ConcurrentHashMap.newKeySet<Path>()

    init {
        scope.launch { runWatcher() }
    }

    /**
     * Resolve (or create + watch) a config of type [T].
     *
     * @param extensionId required for [ConfigType.Extension] when not set on the annotation
     * @param writeDefault if true and file is missing, try to copy a classpath default
     */
    inline fun <reified T : Any> config(
        extensionId: String? = null,
        writeDefault: Boolean = true,
    ): T = config(T::class, extensionId, writeDefault)

    fun <T : Any> config(
        kClass: KClass<T>,
        extensionId: String? = null,
        writeDefault: Boolean = true,
    ): T {
        val annotation = kClass.findAnnotation<ConfigFile>()
            ?: error("${kClass.qualifiedName} is missing @ConfigFile")

        val resolvedExtensionId = when (annotation.type) {
            ConfigType.Root -> null
            ConfigType.Extension -> {
                val id = extensionId?.takeIf { it.isNotBlank() }
                    ?: annotation.extensionId.takeIf { it.isNotBlank() }
                    ?: error(
                        "${kClass.qualifiedName} is ConfigType.Extension but no extensionId " +
                            "was provided (annotation or argument)"
                    )
                id
            }
        }

        val key = ConfigKey(kClass, resolvedExtensionId)

        @Suppress("UNCHECKED_CAST")
        val existing = nodes[key] as ConfigNode<T>?
        if (existing != null) return existing.value

        val file = resolveFile(annotation, resolvedExtensionId)
        ensureParent(file)

        val loaded = loadOrDefault(kClass, file, annotation, writeDefault)
        val node = ConfigNode(loaded, file, kClass)
        nodes[key] = node
        watchDirectory(file.toPath().parent)

        return node.value
    }

    /** Force reload of a known config (or no-op if not registered yet). */
    fun <T : Any> reload(kClass: KClass<T>, extensionId: String? = null): T? {
        val key = ConfigKey(kClass, extensionId)
        @Suppress("UNCHECKED_CAST")
        val node = nodes[key] as ConfigNode<T>? ?: return null
        val result = decodeFile(kClass, node.file)
        if (result is ConfigLoadResult.Loaded<*>) {
            @Suppress("UNCHECKED_CAST")
            node.value = applyOverlays(kClass, result.data as T)
            logger.info("Reloaded config ${kClass.simpleName} from ${node.file.absolutePath}")
            return node.value
        }
        return node.value
    }

    /** Startup helper: register all known root configs so defaults are written early. */
    suspend fun loadConfigsStartup(vararg classes: KClass<*>) {
        logger.info("Loading configs from ${directory.toAbsolutePath()}")
        classes.forEach { kClass ->
            @Suppress("UNCHECKED_CAST")
            config(kClass as KClass<Any>, writeDefault = true)
        }
    }

    private fun resolveFile(annotation: ConfigFile, extensionId: String?): File {
        val fileName = "${annotation.name}.jsonc"
        return when (annotation.type) {
            ConfigType.Root -> (directory / fileName).toFile()
            ConfigType.Extension -> {
                requireNotNull(extensionId)
                (directory / "ext" / extensionId / fileName).toFile()
            }
        }
    }

    private fun ensureParent(file: File) {
        val parent = file.parentFile ?: return
        if (!parent.exists()) parent.mkdirs()
    }

    private fun <T : Any> loadOrDefault(
        kClass: KClass<T>,
        file: File,
        annotation: ConfigFile,
        writeDefault: Boolean,
    ): T {
        when (val result = decodeFile(kClass, file)) {
            is ConfigLoadResult.Loaded<*> -> {
                @Suppress("UNCHECKED_CAST")
                return applyOverlays(kClass, result.data as T)
            }
            is ConfigLoadResult.Failure -> {
                when (result) {
                    ConfigLoadResult.Failure.NotFound -> {
                        logger.warn("Config file not found: ${file.absolutePath}")
                        if (writeDefault) {
                            val resource = defaultResourcePath(annotation, extensionId = null)
                            try {
                                // best-effort write from classpath; fall through to empty instance if missing
                                writeDefaultFromResource(file, resource)
                                when (val again = decodeFile(kClass, file)) {
                                    is ConfigLoadResult.Loaded<*> -> {
                                        @Suppress("UNCHECKED_CAST")
                                        return applyOverlays(kClass, again.data as T)
                                    }
                                    else -> Unit
                                }
                            } catch (e: Exception) {
                                logger.warn("Could not write default for ${kClass.simpleName}: ${e.message}")
                            }
                        }
                    }
                    ConfigLoadResult.Failure.DecodeError -> {
                        logger.error("Failed to decode ${file.absolutePath}")
                    }
                }
            }
        }

        // Last resort: try no-arg / all-default primary constructor
        return instantiateEmpty(kClass)
            ?: error("Unable to load or construct config ${kClass.qualifiedName}")
    }

    private fun <T : Any> decodeFile(kClass: KClass<T>, file: File): ConfigLoadResult {
        if (!file.exists()) return ConfigLoadResult.Failure.NotFound
        return try {
            val content = file.readText()
            val serializer = json.serializersModule.serializer(kClass.java)
            @Suppress("UNCHECKED_CAST")
            val data = json.decodeFromString(serializer, content) as T
            ConfigLoadResult.Loaded(data)
        } catch (e: Exception) {
            logger.error("Decode error for ${file.absolutePath}: ${e.message}")
            ConfigLoadResult.Failure.DecodeError
        }
    }

    private fun <T : Any> applyOverlays(kClass: KClass<T>, config: T): T {
        for (member in kClass.members.filter { it.hasAnnotation<Environment>() }) {
            val annotation = member.findAnnotation<Environment>() ?: continue
            if (member is KProperty<*>) {
                val javaField = member.javaField ?: continue
                if (javaField.type == String::class.java) {
                    javaField.isAccessible = true
                    System.getenv(annotation.env)?.let { javaField.set(config, it) }
                }
            }
        }

        for (member in kClass.members.filter { it.hasAnnotation<JavaProperty>() }) {
            val annotation = member.findAnnotation<JavaProperty>() ?: continue
            if (member is KProperty<*>) {
                val javaField = member.javaField ?: continue
                if (javaField.type == String::class.java) {
                    javaField.isAccessible = true
                    System.getProperty(annotation.prop)?.let { javaField.set(config, it) }
                }
            }
        }

        return config
    }

    private fun <T : Any> instantiateEmpty(kClass: KClass<T>): T? {
        return try {
            val ctor = kClass.primaryConstructor ?: return null
            val args = ctor.parameters.associateWith { param ->
                when {
                    param.isOptional -> null // use default
                    else -> null
                }
            }.filterValues { it != null }
            // Prefer callBy with only required defaults filled by Kotlin
            ctor.callBy(emptyMap())
        } catch (_: Exception) {
            null
        }
    }

    private fun defaultResourcePath(annotation: ConfigFile, extensionId: String?): String {
        return when (annotation.type) {
            ConfigType.Root -> "/configs/${annotation.name}.default.jsonc"
            ConfigType.Extension ->
                "/configs/ext/${extensionId ?: annotation.extensionId}/${annotation.name}.default.jsonc"
        }
    }

    private fun writeDefaultFromResource(file: File, resource: String) {
        val stream = ConfigService::class.java.getResourceAsStream(resource)
            ?: throw IOException("Missing default resource $resource")
        stream.use { input ->
            file.parentFile?.mkdirs()
            file.outputStream().use { output -> input.copyTo(output) }
        }
        logger.info("Wrote default config to ${file.absolutePath}")
    }

    private fun watchDirectory(dir: Path) {
        if (dir == null || !Files.isDirectory(dir)) return
        if (watchedDirs.add(dir)) {
            try {
                dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                )
                logger.debug("Watching config directory $dir")
            } catch (e: Exception) {
                watchedDirs.remove(dir)
                logger.warn("Could not watch $dir: ${e.message}")
            }
        }
    }

    private suspend fun runWatcher() {
        withContext(Dispatchers.IO) {
            while (true) {
                val key: WatchKey = try {
                    watchService.take()
                } catch (_: InterruptedException) {
                    break
                } catch (_: ClosedWatchServiceException) {
                    break
                }

                val dir = key.watchable() as? Path
                for (event in key.pollEvents()) {
                    val kind = event.kind()
                    if (kind == StandardWatchEventKinds.OVERFLOW) continue
                    val name = event.context() as? Path ?: continue
                    val changed = dir?.resolve(name)?.toFile() ?: continue
                    if (!changed.name.endsWith(".jsonc")) continue

                    nodes.values
                        .filter { it.file.absolutePath == changed.absolutePath }
                        .forEach { node ->
                            reload(node.kClass, extensionIdFor(node))
                        }
                }
                key.reset()
            }
        }
    }

    private fun extensionIdFor(node: ConfigNode<*>): String? {
        val annotation = node.kClass.findAnnotation<ConfigFile>() ?: return null
        return if (annotation.type == ConfigType.Extension) {
            nodes.entries.find { it.value === node }?.key?.extensionId
        } else null
    }

    fun close() {
        try {
            watchService.close()
        } catch (_: Exception) {
        }
    }
}

private typealias ClosedWatchServiceException = java.nio.file.ClosedWatchServiceException
