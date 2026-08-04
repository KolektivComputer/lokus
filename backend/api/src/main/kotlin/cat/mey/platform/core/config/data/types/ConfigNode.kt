package cat.mey.platform.core.config.data.types

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import kotlin.reflect.KClass

/**
 * A watched config file backed by a [StateFlow] so reads/updates are coroutine-safe.
 */
sealed interface ConfigNode<T : Any> {
    val kClass: KClass<T>
    val file: File

    /** Current snapshot; safe to read from any context. */
    val value: T

    /** Observe live updates (e.g. after file change). */
    val state: StateFlow<T>

    /** Replace the current value under the node mutex. */
    suspend fun update(newValue: T)
}

class RootConfigNode<T : Any>(
    override val kClass: KClass<T>,
    override val file: File,
    initial: T,
) : ConfigNode<T> {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(initial)

    override val state: StateFlow<T> = _state.asStateFlow()
    override val value: T get() = _state.value

    override suspend fun update(newValue: T) {
        mutex.withLock {
            _state.value = newValue
        }
    }
}

class ExtensionConfigNode<T : Any>(
    val extensionId: String,
    override val kClass: KClass<T>,
    override val file: File,
    initial: T,
) : ConfigNode<T> {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(initial)

    override val state: StateFlow<T> = _state.asStateFlow()
    override val value: T get() = _state.value

    override suspend fun update(newValue: T) {
        mutex.withLock {
            _state.value = newValue
        }
    }
}
