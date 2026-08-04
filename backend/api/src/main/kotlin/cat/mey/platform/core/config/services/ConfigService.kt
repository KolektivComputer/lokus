package cat.mey.platform.core.config.services

import cat.mey.platform.core.config.ConfigFile
import cat.mey.platform.core.config.ConfigType
import cat.mey.platform.core.config.Environment
import cat.mey.platform.core.config.JavaProperty
import cat.mey.platform.core.config.data.types.ConfigNode
import cat.mey.platform.core.config.data.types.ExtensionConfigNode
import cat.mey.platform.core.config.data.types.RootConfigNode
import cat.mey.platform.core.config.data.types.results.ConfigLoadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.div
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Annotation-driven config loader with file watching.
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
 *
 * Values live in [RootConfigNode] / [ExtensionConfigNode] behind [StateFlow],
 * so concurrent readers and reloaders stay coroutine-safe.
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

    /** All registered nodes (root + extension). */
    private val nodes = CopyOnWriteArrayList<ConfigNode<*>>()

    /** Guards get-or-create so two coroutines don't double-register the same config. */
    private val registryMutex = Mutex()

    private val watchService = FileSystems.getDefault().newWatchService()
    private val watchedDirs = java.util.concurrent.ConcurrentHashMap.newKeySet<Path>()

    init {
        scope.launch { runWatcher() }
    }

    /**
     * Resolve (or create + watch) a config of type [T].
     *
     * @param extensionId required for [ConfigType.Extension] when not set on the annotation
     * @param writeDefault if true and file is missing, try classpath default / data-class defaults
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
        // Non-suspend Koin factories call this; get-or-create is synchronized via runBlocking + mutex.
        return runBlocking {
            getOrCreateNode(kClass, extensionId, writeDefault).value
        }
    }

    /** Same as [config] but returns the live [StateFlow] for collectors. */
    inline fun <reified T : Any> configState(
        extensionId: String? = null,
        writeDefault: Boolean = true,
    ) = configState(T::class, extensionId, writeDefault)

    fun <T : Any> configState(
        kClass: KClass<T>,
        extensionId: String? = null,
        writeDefault: Boolean = true,
    ) = runBlocking {
        getOrCreateNode(kClass, extensionId, writeDefault).state
    }

    /** Force reload of a known config (or no-op if not registered yet). */
    suspend fun <T : Any> reload(kClass: KClass<T>, extensionId: String? = null): T? {
        val node = findNode(kClass, extensionId) ?: return null
        val result = decodeFile(kClass, node.file)
        if (result is ConfigLoadResult.Loaded<*>) {
            @Suppress("UNCHECKED_CAST")
            val updated = applyOverlays(kClass, result.data as T)
            node.update(updated)
            logger.info("Reloaded config ${kClass.simpleName} from ${node.file.absolutePath}")
            return updated
        }
        return node.value
    }

    /** Startup helper: register root configs so defaults exist before the rest of the app boots. */
    suspend fun loadConfigsStartup(vararg classes: KClass<*>) {
        logger.info("Loading configs from ${directory.toAbsolutePath()}")
        classes.forEach { kClass ->
            @Suppress("UNCHECKED_CAST")
            getOrCreateNode(kClass as KClass<Any>, extensionId = null, writeDefault = true)
        }
    }

    // -------------------------------------------------------------------------
    // Registry / resolution
    // -------------------------------------------------------------------------

    private suspend fun <T : Any> getOrCreateNode(
        kClass: KClass<T>,
        extensionId: String?,
        writeDefault: Boolean,
    ): ConfigNode<T> {
        val annotation = kClass.findAnnotation<ConfigFile>()
            ?: error("${kClass.qualifiedName} is missing @ConfigFile")

        val resolvedExtensionId = resolveExtensionId(kClass, annotation, extensionId)

        registryMutex.withLock {
            findNode(kClass, resolvedExtensionId)?.let { return it }

            val file = resolveFile(annotation, resolvedExtensionId)
            ensureParent(file)

            val loaded = loadOrDefault(kClass, file, annotation, resolvedExtensionId, writeDefault)

            val node: ConfigNode<T> = when (annotation.type) {
                ConfigType.Root -> RootConfigNode(
                    kClass = kClass,
                    file = file,
                    initial = loaded,
                )
                ConfigType.Extension -> ExtensionConfigNode(
                    extensionId = requireNotNull(resolvedExtensionId),
                    kClass = kClass,
                    file = file,
                    initial = loaded,
                )
            }

            nodes += node
            file.toPath().parent?.let { watchDirectory(it) }

            return node
        }
    }

    /**
     * Lookup existing node.
     *
     * If an extension id is present (from argument or annotation), only
     * [ExtensionConfigNode]s with that id are considered; otherwise only
     * [RootConfigNode]s.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> findNode(
        kClass: KClass<T>,
        extensionId: String?,
    ): ConfigNode<T>? {
        return if (extensionId != null) {
            nodes
                .filterIsInstance<ExtensionConfigNode<*>>()
                .filter { it.extensionId == extensionId && it.kClass == kClass }
                .firstOrNull() as ConfigNode<T>?
        } else {
            nodes
                .filterIsInstance<RootConfigNode<*>>()
                .filter { it.kClass == kClass }
                .firstOrNull() as ConfigNode<T>?
        }
    }

    private fun resolveExtensionId(
        kClass: KClass<*>,
        annotation: ConfigFile,
        extensionId: String?,
    ): String? {
        return when (annotation.type) {
            ConfigType.Root -> null
            ConfigType.Extension -> {
                extensionId?.takeIf { it.isNotBlank() }
                    ?: annotation.extensionId.takeIf { it.isNotBlank() }
                    ?: error(
                        "${kClass.qualifiedName} is ConfigType.Extension but no extensionId " +
                            "was provided (annotation or argument)"
                    )
            }
        }
    }

    // -------------------------------------------------------------------------
    // File IO
    // -------------------------------------------------------------------------

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
        file.parentFile?.takeUnless { it.exists() }?.mkdirs()
    }

    private fun <T : Any> loadOrDefault(
        kClass: KClass<T>,
        file: File,
        annotation: ConfigFile,
        extensionId: String?,
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
                            val resource = defaultResourcePath(annotation, extensionId)
                            try {
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

    // -------------------------------------------------------------------------
    // Watching
    // -------------------------------------------------------------------------

    private fun watchDirectory(dir: Path) {
        if (!Files.isDirectory(dir)) return
        if (!watchedDirs.add(dir)) return
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
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue
                    val name = event.context() as? Path ?: continue
                    val changed = dir?.resolve(name)?.toFile() ?: continue
                    if (!changed.name.endsWith(".jsonc")) continue

                    nodes
                        .filter { it.file.absolutePath == changed.absolutePath }
                        .forEach { node ->
                            val extId = (node as? ExtensionConfigNode<*>)?.extensionId
                            scope.launch {
                                reload(node.kClass, extId)
                            }
                        }
                }
                key.reset()
            }
        }
    }

    fun close() {
        try {
            watchService.close()
        } catch (_: Exception) {
        }
    }
}
