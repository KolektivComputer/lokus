package capital.yuri.locus.platform.core.config.services

import capital.yuri.locus.common.resource.ResourceLoadResult
import capital.yuri.locus.common.resource.ResourceLoader
import capital.yuri.locus.platform.core.config.Config
import capital.yuri.locus.platform.core.config.ConfigCodec
import capital.yuri.locus.platform.core.config.ConfigLocation
import capital.yuri.locus.platform.core.config.ConfigResolveContext
import capital.yuri.locus.platform.core.config.ConfigScope
import capital.yuri.locus.platform.core.config.Environment
import capital.yuri.locus.platform.core.config.JavaProperty
import capital.yuri.locus.platform.core.config.data.types.ConfigNode
import capital.yuri.locus.platform.core.config.data.types.ExtensionConfigNode
import capital.yuri.locus.platform.core.config.data.types.RootConfigNode
import capital.yuri.locus.platform.core.config.data.types.results.ConfigLoadResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Factory + registry for [ConfigNode]s.
 *
 * ```
 * private val db by configService.config<DatabaseConfig>()
 * private val shop by configService.config<ShopConfig>() // extensionId from @Config
 * ```
 */
class ConfigService(private val directory: Path) : KoinComponent {
    private val resourceLoader by inject<ResourceLoader>()
    private val logger = LoggerFactory.getLogger(ConfigService::class.java)

    private val nodes = CopyOnWriteArrayList<ConfigNode<*>>()
    private val registryMutex = Mutex()

    inline fun <reified T : Any> config(extensionId: String? = null, writeDefault: Boolean = true): ConfigNode<T> =
        config(T::class, extensionId, writeDefault)

    fun <T : Any> config(kClass: KClass<T>, extensionId: String? = null, writeDefault: Boolean = true): ConfigNode<T> =
        runBlocking { getOrCreateNode(kClass, extensionId, writeDefault) }

    suspend fun loadConfigsStartup(vararg classes: KClass<*>) {
        logger.info("Loading configs from {}", directory.toAbsolutePath())
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

    private suspend fun <T : Any> getOrCreateNode(
        kClass: KClass<T>,
        extensionId: String?,
        writeDefault: Boolean,
    ): ConfigNode<T> {
        val annotation = kClass.findAnnotation<Config>()
            ?: error("${kClass.qualifiedName} is missing @Config")

        val resolvedExtensionId = resolveExtensionId(kClass, annotation, extensionId)
        val ctx = ConfigResolveContext(
            name = annotation.name,
            scope = annotation.scope,
            extensionId = resolvedExtensionId,
            configDirectory = directory,
        )

        registryMutex.withLock {
            findNode(kClass, resolvedExtensionId)?.let { return it }

            val loaded = loadConfig(kClass, annotation, ctx, writeDefault)
            val primaryFile = annotation.location.fileCandidates(ctx).firstOrNull()
                ?: File(directory.toFile(), "${annotation.name}.jsonc")

            val reloadFn: suspend (File) -> T? = { f ->
                when (val result = decodeFile(kClass, f)) {
                    is ConfigLoadResult.Loaded<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        applyOverlays(kClass, result.data as T)
                    }
                    else -> null
                }
            }

            val node: ConfigNode<T> = when (annotation.scope) {
                ConfigScope.Root -> RootConfigNode(
                    kClass = kClass,
                    file = primaryFile,
                    initial = loaded,
                    reload = reloadFn,
                )
                ConfigScope.Extension -> ExtensionConfigNode(
                    extensionId = requireNotNull(resolvedExtensionId),
                    kClass = kClass,
                    file = primaryFile,
                    initial = loaded,
                    reload = reloadFn,
                )
            }

            nodes += node
            return node
        }
    }

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

    private fun resolveExtensionId(kClass: KClass<*>, annotation: Config, extensionId: String?): String? =
        when (annotation.scope) {
            ConfigScope.Root -> null
            ConfigScope.Extension ->
                extensionId?.takeIf { it.isNotBlank() }
                    ?: annotation.extensionId.takeIf { it.isNotBlank() }
                    ?: error(
                        "${kClass.qualifiedName} is ConfigScope.Extension but no extensionId " +
                            "was provided (annotation or argument)",
                    )
        }

    private fun <T : Any> loadConfig(
        kClass: KClass<T>,
        annotation: Config,
        ctx: ConfigResolveContext,
        writeDefault: Boolean,
    ): T {
        when (annotation.location) {
            ConfigLocation.File -> {
                for (file in annotation.location.fileCandidates(ctx)) {
                    when (val result = decodeFile(kClass, file)) {
                        is ConfigLoadResult.Loaded<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            return applyOverlays(kClass, result.data as T)
                        }
                        ConfigLoadResult.Failure.DecodeError ->
                            logger.error("Failed to decode {}", file.absolutePath)
                        ConfigLoadResult.Failure.NotFound -> Unit
                    }
                }

                logger.warn("Config file not found for '{}' under {}", ctx.name, directory)
                if (writeDefault) {
                    val target = annotation.location.fileCandidates(ctx).first()
                    val defaults = ConfigLocation.defaultResourceCandidates(ctx)
                    for (resource in defaults) {
                        when (val copied = resourceLoader.copyToFile(resource, target)) {
                            is ResourceLoadResult.Ok -> {
                                when (val again = decodeFile(kClass, target)) {
                                    is ConfigLoadResult.Loaded<*> -> {
                                        @Suppress("UNCHECKED_CAST")
                                        return applyOverlays(kClass, again.data as T)
                                    }
                                    else -> Unit
                                }
                            }
                            is ResourceLoadResult.NotFound -> Unit
                            is ResourceLoadResult.DecodeError ->
                                logger.warn("Default resource {}: {}", resource, copied.message)
                        }
                    }
                }
            }

            ConfigLocation.Resource -> {
                when (val text = resourceLoader.loadTextFirst(annotation.location.resourceCandidates(ctx))) {
                    is ResourceLoadResult.Ok -> {
                        return try {
                            applyOverlays(kClass, ConfigCodec.decode(kClass, text.value))
                        } catch (e: Exception) {
                            logger.error("Resource config decode error: {}", e.message)
                            instantiateEmpty(kClass)
                                ?: error("Unable to load or construct config ${kClass.qualifiedName}")
                        }
                    }
                    is ResourceLoadResult.NotFound ->
                        logger.warn("Resource config not found for '{}'", ctx.name)
                    is ResourceLoadResult.DecodeError ->
                        logger.error("Resource config decode error: {}", text.message)
                }
            }
        }

        return instantiateEmpty(kClass)
            ?: error("Unable to load or construct config ${kClass.qualifiedName}")
    }

    private fun <T : Any> decodeFile(kClass: KClass<T>, file: File): ConfigLoadResult {
        if (!file.exists()) return ConfigLoadResult.Failure.NotFound
        return try {
            val data = ConfigCodec.decode(kClass, file.readText())
            ConfigLoadResult.Loaded(data)
        } catch (e: Exception) {
            logger.error("Decode error for {}: {}", file.absolutePath, e.message)
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

    fun close() {
        nodes.forEach { it.close() }
        nodes.clear()
    }
}
