package capital.yuri.locus.platform.core.config.services

import capital.yuri.locus.platform.core.config.ConfigFile
import capital.yuri.locus.platform.core.config.ConfigType
import capital.yuri.locus.platform.core.config.Environment
import capital.yuri.locus.platform.core.config.JavaProperty
import capital.yuri.locus.platform.core.config.data.types.ConfigNode
import capital.yuri.locus.platform.core.config.data.types.ExtensionConfigNode
import capital.yuri.locus.platform.core.config.data.types.RootConfigNode
import capital.yuri.locus.platform.core.config.data.types.results.ConfigLoadResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.div
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Factory + registry for [ConfigNode]s.
 *
 * Nodes watch their own files and expose values via property delegate:
 * ```
 * private val db by configService.config<DatabaseConfig>()
 * private val shop by configService.config<ShopConfig>() // extensionId from @ConfigFile
 * ```
 */
class ConfigService(private val directory: Path) : KoinComponent {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        allowComments = true
    }

    private val logger = LoggerFactory.getLogger(ConfigService::class.java)

    private val nodes = CopyOnWriteArrayList<ConfigNode<*>>()
    private val registryMutex = Mutex()

    /**
     * Returns a [ConfigNode] usable as a property delegate (`by`).
     * Creates + starts watching on first resolve; subsequent calls reuse the same node.
     */
    inline fun <reified T : Any> config(extensionId: String? = null, writeDefault: Boolean = true): ConfigNode<T> =
        config(T::class, extensionId, writeDefault)

    fun <T : Any> config(kClass: KClass<T>, extensionId: String? = null, writeDefault: Boolean = true): ConfigNode<T> =
        runBlocking {
            getOrCreateNode(kClass, extensionId, writeDefault)
        }

    /** Eagerly register root configs at startup. */
    suspend fun loadConfigsStartup(vararg classes: KClass<*>) {
        logger.info("Loading configs from ${directory.toAbsolutePath()}")
        classes.forEach { kClass ->
            @Suppress("UNCHECKED_CAST")
            getOrCreateNode(kClass as KClass<Any>, extensionId = null, writeDefault = true)
        }
    }

    suspend fun <T : Any> reload(kClass: KClass<T>, extensionId: String? = null): T? {
        val node = findNode(kClass, extensionId) ?: return null
        node.refresh()
        return node.value
    }

    // -------------------------------------------------------------------------
    // Registry
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

            val reloadFn: suspend (File) -> T? = { f ->
                when (val result = decodeFile(kClass, f)) {
                    is ConfigLoadResult.Loaded<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        applyOverlays(kClass, result.data as T)
                    }

                    else -> null
                }
            }

            val node: ConfigNode<T> = when (annotation.type) {
                ConfigType.Root -> RootConfigNode(
                    kClass = kClass,
                    file = file,
                    initial = loaded,
                    reload = reloadFn,
                )

                ConfigType.Extension -> ExtensionConfigNode(
                    extensionId = requireNotNull(resolvedExtensionId),
                    kClass = kClass,
                    file = file,
                    initial = loaded,
                    reload = reloadFn,
                )
            }

            nodes += node
            return node
        }
    }

    /**
     * If [extensionId] is set (arg or annotation), only [ExtensionConfigNode]s with that id.
     * Otherwise only [RootConfigNode]s.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> findNode(kClass: KClass<T>, extensionId: String?): ConfigNode<T>? =
        if (extensionId != null) {
            nodes
                .filterIsInstance<ExtensionConfigNode<*>>()
                .firstOrNull { it.extensionId == extensionId && it.kClass == kClass } as ConfigNode<T>?
        } else {
            nodes
                .filterIsInstance<RootConfigNode<*>>()
                .firstOrNull { it.kClass == kClass } as ConfigNode<T>?
        }

    private fun resolveExtensionId(kClass: KClass<*>, annotation: ConfigFile, extensionId: String?): String? =
        when (annotation.type) {
            ConfigType.Root -> null

            ConfigType.Extension ->
                extensionId?.takeIf { it.isNotBlank() }
                    ?: annotation.extensionId.takeIf { it.isNotBlank() }
                    ?: error(
                        "${kClass.qualifiedName} is ConfigType.Extension but no extensionId " +
                            "was provided (annotation or argument)",
                    )
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

                    ConfigLoadResult.Failure.DecodeError ->
                        logger.error("Failed to decode ${file.absolutePath}")
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

    private fun <T : Any> instantiateEmpty(kClass: KClass<T>): T? = try {
        kClass.primaryConstructor?.callBy(emptyMap())
    } catch (_: Exception) {
        null
    }

    private fun defaultResourcePath(annotation: ConfigFile, extensionId: String?): String = when (annotation.type) {
        ConfigType.Root -> "/configs/${annotation.name}.default.jsonc"

        ConfigType.Extension ->
            "/configs/ext/${extensionId ?: annotation.extensionId}/${annotation.name}.default.jsonc"
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

    fun close() {
        nodes.forEach { it.close() }
        nodes.clear()
    }
}
