package cat.mey.platform.core.config.services

import cat.mey.platform.api.data.config.ApiConfig
import cat.mey.platform.core.auth.data.config.AuthConfig
import cat.mey.platform.core.config.Environment
import cat.mey.platform.core.config.JavaProperty
import cat.mey.platform.core.config.data.types.results.ConfigLoadResult
import cat.mey.platform.core.db.data.config.DatabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaField

class ConfigService(
    private val directory: Path
) : KoinComponent {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        allowComments = true
    }

    private val logger = LoggerFactory.getLogger(ConfigService::class.java)

    lateinit var database: DatabaseConfig
    lateinit var auth: AuthConfig
    lateinit var api: ApiConfig

    suspend fun loadConfigsStartup() {
        logger.info("Loading configs.")
        loadDatabase(true)
        loadAuth(true)
        loadApi(true)
    }

    suspend fun loadDatabase(writeDefault: Boolean = false) {
        val file = (directory / "database.jsonc").toFile()
        logger.info("Loading database config from ${file.absolutePath}.")
        when (val result = loadConfig<DatabaseConfig>(file)) {
            is ConfigLoadResult.Loaded<*> -> database = result.data as DatabaseConfig
            is ConfigLoadResult.Failure -> {
                when (result) {
                    ConfigLoadResult.Failure.NotFound -> {
                        logger.warn("Database config file not found: ${file.absolutePath}")

                        if (writeDefault) tryWriteDefault(file, "/configs/database.default.jsonc")
                    }
                }
            }
        }
    }

    suspend fun loadAuth(writeDefault: Boolean = false) {
        val file = (directory / "auth.jsonc").toFile()
        logger.info("Loading auth config from ${file.absolutePath}.")
        when (val result = loadConfig<AuthConfig>(file)) {
            is ConfigLoadResult.Loaded<*> -> auth = result.data as AuthConfig
            is ConfigLoadResult.Failure -> {
                when (result) {
                    ConfigLoadResult.Failure.NotFound -> {
                        logger.warn("Auth config file not found: ${file.absolutePath}")

                        if (writeDefault) tryWriteDefault(file, "/configs/auth.default.jsonc")
                    }
                }
            }
        }
    }

    suspend fun loadApi(writeDefault: Boolean = false) {
        val file = (directory / "api.jsonc").toFile()
        logger.info("Loading api config from ${file.absolutePath}.")
        when (val result = loadConfig<ApiConfig>(file)) {
            is ConfigLoadResult.Loaded<*> -> api = result.data as ApiConfig
            is ConfigLoadResult.Failure -> {
                when (result) {
                    ConfigLoadResult.Failure.NotFound -> {
                        logger.warn("Api config file not found: ${file.absolutePath}")

                        if (writeDefault) tryWriteDefault(file, "/configs/api.default.jsonc")
                    }
                }
            }
        }
    }

    private inline fun <reified TConfig> loadConfig(file: File): ConfigLoadResult {
        if (file.exists()) {
            val content = file.readText()
            val config = json.decodeFromString<TConfig>(content)

            for (member in TConfig::class.members.filter { it.hasAnnotation<Environment>() }) {
                val annotation = member.findAnnotation<Environment>() ?: continue
                if (member is KProperty<*>) {
                    val javaField = member.javaField ?: continue
                    if (javaField.type == String::class.java)
                        javaField.set(config, System.getenv(annotation.env))
                }
            }

            for (member in TConfig::class.members.filter { it.hasAnnotation<JavaProperty>() }) {
                val annotation = member.findAnnotation<JavaProperty>() ?: continue
                if (member is KProperty<*>) {
                    val javaField = member.javaField ?: continue
                    if (javaField.type == String::class.java)
                        javaField.set(config, System.getProperty(annotation.prop))
                }
            }

            return ConfigLoadResult.Loaded(config)
        } else return ConfigLoadResult.Failure.NotFound
    }

    private suspend fun tryWriteDefault(file: File, resource: String) {
        try {
            writeDefault(file, resource)
            logger.info("Default config written to ${file.absolutePath}.")
        } catch (e: Exception) {
            logger.error("Could not write default config file to ${file.absolutePath}: ${e.message}")
        }
    }

    /**
     * Write a default config from JAR resources
     *
     * @throws IOException
     * @throws OutOfMemoryError
     */
    private suspend fun writeDefault(file: File, resource: String) {
        logger.info("Writing default config file to ${file.absolutePath}.")
        withContext(Dispatchers.IO) {
            file.createNewFile()

            val defaultBytes = ConfigService::class
                .java
                .getResourceAsStream(resource)
                ?.readAllBytes()

            if (defaultBytes == null)
                throw IOException("Failed to read resource $resource")

            file.writeBytes(defaultBytes)
        }
    }
}