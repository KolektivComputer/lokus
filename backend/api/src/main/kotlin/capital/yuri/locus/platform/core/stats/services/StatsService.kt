package capital.yuri.locus.platform.core.stats.services

import capital.yuri.locus.platform.core.stats.StatisticGroupProvider
import capital.yuri.locus.platform.core.stats.data.types.StatisticGroupId
import capital.yuri.locus.platform.core.stats.data.types.groups.BuildVersion
import capital.yuri.locus.platform.core.stats.data.types.groups.DatabaseStatGroup
import capital.yuri.locus.platform.core.stats.data.types.groups.InstanceStatGroup
import capital.yuri.locus.platform.core.stats.data.types.groups.RuntimeStatGroup
import capital.yuri.locus.platform.core.stats.data.types.groups.SchedulerStatGroup
import capital.yuri.locus.platform.core.stats.data.types.results.GetStatGroupResult
import capital.yuri.locus.platform.core.scheduling.services.SchedulerService
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Instant

class StatsService : KoinComponent {
    private val logger = LoggerFactory.getLogger(StatsService::class.java)
    private val schedulerService by inject<SchedulerService>()

    private val startedAt: Instant = Clock.System.now()
    private val providers = ConcurrentHashMap<String, StatisticGroupProvider>()

    private val buildVersion: BuildVersion? by lazy { loadBuildVersion() }

    init {
        register(instanceProvider())
        register(runtimeProvider())
        register(databaseProvider())
        register(schedulerProvider())
    }

    fun register(provider: StatisticGroupProvider) {
        providers[provider.id.value] = provider
    }

    fun unregister(id: StatisticGroupId) {
        providers.remove(id.value)
    }

    fun listGroupIds(): List<StatisticGroupId> =
        providers.keys.sorted().map { StatisticGroupId(it) }

    suspend fun getGroup(id: StatisticGroupId): GetStatGroupResult {
        val provider = providers[id.value]
            ?: return GetStatGroupResult.NotFound(id)
        return try {
            provider.collect()
        } catch (e: Exception) {
            logger.warn("Stat group '{}' failed: {}", id.value, e.message)
            GetStatGroupResult.Failed(id, e.message ?: e::class.simpleName ?: "error")
        }
    }

    suspend fun getAll(): Map<String, GetStatGroupResult> =
        listGroupIds().associate { it.value to getGroup(it) }

    // ------------------------------------------------------------------------- 
    // Core providers
    // -------------------------------------------------------------------------

    private fun instanceProvider() = StatisticGroupProvider {
        val now = Clock.System.now()
        GetStatGroupResult.SuccessInstance(
            data = InstanceStatGroup(
                uptimeSeconds = (now - startedAt).inWholeSeconds,
                startedAtEpochMs = startedAt.toEpochMilliseconds(),
                version = buildVersion,
            ),
        )
    }.withId(StatisticGroupId.Instance)

    private fun runtimeProvider() = StatisticGroupProvider {
        val heap = ManagementFactory.getMemoryMXBean().heapMemoryUsage
        val threads = ManagementFactory.getThreadMXBean()
        val rt = ManagementFactory.getRuntimeMXBean()
        GetStatGroupResult.SuccessRuntime(
            data = RuntimeStatGroup(
                heapUsedBytes = heap.used,
                heapMaxBytes = heap.max,
                heapCommittedBytes = heap.committed,
                threadCount = threads.threadCount,
                daemonThreadCount = threads.daemonThreadCount,
                peakThreadCount = threads.peakThreadCount,
                activeCoroutines = probeActiveCoroutines(),
                availableProcessors = Runtime.getRuntime().availableProcessors(),
                jvmUptimeMs = rt.uptime,
            ),
        )
    }.withId(StatisticGroupId.Runtime)

    private fun databaseProvider() = StatisticGroupProvider {
        GetStatGroupResult.SuccessDatabase(data = pingDatabase())
    }.withId(StatisticGroupId.Database)

    private fun schedulerProvider() = StatisticGroupProvider {
        GetStatGroupResult.SuccessScheduler(data = schedulerService.stats())
    }.withId(StatisticGroupId.Scheduler)

    private fun pingDatabase(): DatabaseStatGroup {
        val start = System.nanoTime()
        return try {
            transaction {
                exec("SELECT 1") { rs ->
                    rs.next()
                }
            }
            val latencyMs = (System.nanoTime() - start) / 1_000_000
            DatabaseStatGroup.Ok(latencyMs = latencyMs)
        } catch (e: Exception) {
            DatabaseStatGroup.Unavailable(message = e.message ?: "database unreachable")
        }
    }

    private fun probeActiveCoroutines(): Long? = try {
        val probes = Class.forName("kotlinx.coroutines.debug.internal.DebugProbesImpl")
        val instance = probes.getField("INSTANCE").get(null)
        val dump = probes.getMethod("dumpCoroutinesInfo").invoke(instance) as? List<*>
        dump?.size?.toLong()
    } catch (_: Throwable) {
        null
    }

    private fun loadBuildVersion(): BuildVersion? = try {
        val stream = StatsService::class.java.getResourceAsStream("/version.json") ?: return null
        stream.use {
            Json { ignoreUnknownKeys = true }.decodeFromString(BuildVersion.serializer(), it.bufferedReader().readText())
        }
    } catch (e: Exception) {
        logger.debug("No version.json: {}", e.message)
        null
    }

    private fun StatisticGroupProvider.withId(id: StatisticGroupId): StatisticGroupProvider =
        object : StatisticGroupProvider {
            override val id: StatisticGroupId = id
            override suspend fun collect(): GetStatGroupResult = this@withId.collect()
        }
}

// Allow fun-interface style with explicit id via extension above
private fun StatisticGroupProvider(block: suspend () -> GetStatGroupResult) =
    object : StatisticGroupProvider {
        override val id: StatisticGroupId = StatisticGroupId("_")
        override suspend fun collect(): GetStatGroupResult = block()
    }
