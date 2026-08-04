package capital.yuri.locus.platform.core.stats.data.types.groups

import kotlinx.serialization.Serializable

@Serializable
data class RuntimeStatGroup(
    val heapUsedBytes: Long,
    val heapMaxBytes: Long,
    val heapCommittedBytes: Long,
    val threadCount: Int,
    val daemonThreadCount: Int,
    val peakThreadCount: Int,
    /** Null when coroutine debug probes are not installed. */
    val activeCoroutines: Long? = null,
    val availableProcessors: Int,
    val jvmUptimeMs: Long,
)
