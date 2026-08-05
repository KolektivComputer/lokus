package capital.yuri.locus.platform.core.config

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Multi-format kotlinx.serialization helpers for config documents.
 * Prefer jsonc (comments allowed), then json.
 */
object ConfigCodec {
    val EXTENSIONS: List<String> = listOf("jsonc", "json")

    val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        allowComments = true
        encodeDefaults = true
    }

    fun <T : Any> decode(kClass: KClass<T>, text: String): T {
        val serializer = json.serializersModule.serializer(kClass.java)
        @Suppress("UNCHECKED_CAST")
        return json.decodeFromString(serializer, text) as T
    }

    inline fun <reified T : Any> decode(text: String): T =
        json.decodeFromString(serializer<T>(), text)

    fun <T : Any> encode(kClass: KClass<T>, value: T): String {
        val serializer = json.serializersModule.serializer(kClass.java)
        @Suppress("UNCHECKED_CAST")
        return json.encodeToString(serializer as SerializationStrategy<T>, value)
    }

    fun extensionOf(path: String): String =
        path.substringAfterLast('.', missingDelimiterValue = "jsonc")
}
