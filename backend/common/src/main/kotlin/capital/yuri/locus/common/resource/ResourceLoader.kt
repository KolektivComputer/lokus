package capital.yuri.locus.common.resource

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.slf4j.LoggerFactory
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Classpath resource reader with typed kotlinx.serialization decode.
 */
class ResourceLoader(
    private val classLoader: ClassLoader = ResourceLoader::class.java.classLoader,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        allowComments = true
        prettyPrint = true
        encodeDefaults = true
    },
) {
    private val logger = LoggerFactory.getLogger(ResourceLoader::class.java)

    fun open(path: String): InputStream? {
        val normalized = if (path.startsWith("/")) path else "/$path"
        return classLoader.getResourceAsStream(normalized.removePrefix("/"))
            ?: classLoader.getResourceAsStream(normalized)
            ?: ResourceLoader::class.java.getResourceAsStream(normalized)
    }

    fun loadText(path: String): ResourceLoadResult<String> {
        val stream = open(path) ?: return ResourceLoadResult.NotFound(path)
        return try {
            stream.use {
                ResourceLoadResult.Ok(it.bufferedReader().readText(), path)
            }
        } catch (e: Exception) {
            ResourceLoadResult.DecodeError(path, e.message ?: "read failed")
        }
    }

    fun loadTextFirst(paths: List<String>): ResourceLoadResult<String> {
        var lastNotFound: ResourceLoadResult.NotFound? = null
        for (path in paths) {
            when (val result = loadText(path)) {
                is ResourceLoadResult.Ok -> return result
                is ResourceLoadResult.NotFound -> lastNotFound = result
                is ResourceLoadResult.DecodeError -> return result
            }
        }
        return lastNotFound ?: ResourceLoadResult.NotFound(paths.firstOrNull() ?: "")
    }

    fun <T : Any> load(kClass: KClass<T>, path: String): ResourceLoadResult<T> =
        when (val text = loadText(path)) {
            is ResourceLoadResult.Ok -> decode(kClass, text.value, path)
            is ResourceLoadResult.NotFound -> text
            is ResourceLoadResult.DecodeError -> text
        }

    inline fun <reified T : Any> load(path: String): ResourceLoadResult<T> =
        load(T::class, path)

    fun <T : Any> loadFirst(kClass: KClass<T>, paths: List<String>): ResourceLoadResult<T> =
        when (val text = loadTextFirst(paths)) {
            is ResourceLoadResult.Ok -> decode(kClass, text.value, text.path)
            is ResourceLoadResult.NotFound -> text
            is ResourceLoadResult.DecodeError -> text
        }

    inline fun <reified T : Any> loadFirst(paths: List<String>): ResourceLoadResult<T> =
        loadFirst(T::class, paths)

    fun copyToFile(resourcePath: String, target: java.io.File): ResourceLoadResult<Unit> {
        val stream = open(resourcePath) ?: return ResourceLoadResult.NotFound(resourcePath)
        return try {
            stream.use { input ->
                target.parentFile?.mkdirs()
                target.outputStream().use { output -> input.copyTo(output) }
            }
            logger.info("Wrote resource {} -> {}", resourcePath, target.absolutePath)
            ResourceLoadResult.Ok(Unit, resourcePath)
        } catch (e: Exception) {
            ResourceLoadResult.DecodeError(resourcePath, e.message ?: "copy failed")
        }
    }

    private fun <T : Any> decode(kClass: KClass<T>, text: String, path: String): ResourceLoadResult<T> =
        try {
            val serializer = json.serializersModule.serializer(kClass.java)
            @Suppress("UNCHECKED_CAST")
            ResourceLoadResult.Ok(json.decodeFromString(serializer, text) as T, path)
        } catch (e: Exception) {
            logger.error("Decode failed for {}: {}", path, e.message)
            ResourceLoadResult.DecodeError(path, e.message ?: "decode failed")
        }
}
