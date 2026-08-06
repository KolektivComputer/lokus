package capital.yuri.locus.platform.core.config.data.types

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * A self-contained, watched config binding.
 *
 * Use as a property delegate:
 * ```
 * private val myConfig by configService.config<MyConfig>()
 * ```
 * [getValue] reads the current [StateFlow] snapshot.
 */
sealed class ConfigNode<T : Any>(
    val kClass: KClass<T>,
    val file: File,
    initial: T,
    private val reload: suspend (File) -> T?,
) {
    private val logger = LoggerFactory.getLogger(ConfigNode::class.java)
    private val mutex = Mutex()
    private val _state = MutableStateFlow(initial)

    val state: StateFlow<T> = _state.asStateFlow()

    /** Current snapshot of the config value. */
    val value: T get() = _state.value

    /** Property-delegate read — `by configService.config<T>()`. */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var watchJob: Job? = null

    init {
        startWatching()
    }

    suspend fun update(newValue: T) {
        mutex.withLock {
            _state.value = newValue
        }
    }

    /** Re-read [file] via the provided reload function and publish if successful. */
    suspend fun refresh() {
        val updated = reload(file) ?: return
        update(updated)
        logger.info("Reloaded ${kClass.simpleName} from ${file.absolutePath}")
    }

    private fun startWatching() {
        val dir = file.parentFile?.toPath() ?: return
        watchJob = scope.launch {
            val watchService = FileSystems.getDefault().newWatchService()
            try {
                dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                )
                while (isActive) {
                    val key = try {
                        watchService.take()
                    } catch (_: ClosedWatchServiceException) {
                        break
                    } catch (_: InterruptedException) {
                        break
                    }

                    for (event in key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue
                        val name = event.context() as? Path ?: continue
                        if (name.fileName.toString() != file.name) continue
                        // brief debounce for editors that write then rename
                        kotlinx.coroutines.delay(50)
                        refresh()
                    }
                    if (!key.reset()) break
                }
            } finally {
                try {
                    watchService.close()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun close() {
        watchJob?.cancel()
        scope.cancel()
    }
}

class RootConfigNode<T : Any>(kClass: KClass<T>, file: File, initial: T, reload: suspend (File) -> T?) :
    ConfigNode<T>(kClass, file, initial, reload)

class ExtensionConfigNode<T : Any>(
    val extensionId: String,
    kClass: KClass<T>,
    file: File,
    initial: T,
    reload: suspend (File) -> T?,
) : ConfigNode<T>(kClass, file, initial, reload)
