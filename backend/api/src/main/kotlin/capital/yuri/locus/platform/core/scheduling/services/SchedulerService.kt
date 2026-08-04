package capital.yuri.locus.platform.core.scheduling.services

import capital.yuri.locus.platform.core.db.services.DatabaseService
import capital.yuri.locus.platform.core.scheduling.KoinJobFactory
import capital.yuri.locus.platform.core.scheduling.RunOnceLaterJobDsl
import capital.yuri.locus.platform.core.stats.data.types.groups.SchedulerStatGroup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.jdbcjobstore.JobStoreSupport
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.utils.ConnectionProvider
import org.quartz.utils.DBConnectionManager
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean
import javax.sql.DataSource

/**
 * Quartz scheduler.
 *
 * After [DatabaseService.connect], call [start] so the JDBC job store can use the
 * shared Hikari pool (no second Quartz-managed datasource).
 *
 * Falls back to RAM if JDBC init fails (e.g. missing `QRTZ_*` tables).
 * Apply Quartz `tables_postgres.sql` via migrate for durable jobs.
 */
class SchedulerService : KoinComponent {
    private val logger = LoggerFactory.getLogger(SchedulerService::class.java)
    private val databaseService by inject<DatabaseService>()

    private val started = AtomicBoolean(false)
    private lateinit var scheduler: Scheduler

    /** Safe accessor — starts with RAM if [start] was never called. */
    fun requireScheduler(): Scheduler {
        if (!started.get()) {
            start()
        }
        return scheduler
    }

    /**
     * Prefer JDBC job store backed by [DatabaseService.dataSource] when the pool
     * is connected; otherwise RAM.
     */
    fun start() {
        if (!started.compareAndSet(false, true)) return

        scheduler = if (databaseService.isConnected) {
            tryStartJdbc(databaseService.dataSource)
                ?: startRam("JDBC job store failed")
        } else {
            startRam("database not connected yet")
        }
    }

    private fun tryStartJdbc(dataSource: DataSource): Scheduler? = try {
        val dsName = QUARTZ_DS_NAME
        DBConnectionManager.getInstance().addConnectionProvider(
            dsName,
            DataSourceConnectionProvider(dataSource),
        )

        val props = commonProperties().apply {
            setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX")
            setProperty(
                "org.quartz.jobStore.driverDelegateClass",
                "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
            )
            setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_")
            setProperty("org.quartz.jobStore.dataSource", dsName)
            setProperty("org.quartz.jobStore.isClustered", "false")
            // Don't let Quartz open its own pool — we registered [dataSource] above.
        }

        createAndStart(props).also {
            logger.info("Quartz using JDBC job store via shared Hikari pool")
        }
    } catch (e: Exception) {
        logger.warn(
            "Quartz JDBC job store failed ({}). Falling back to RAM. " +
                "Apply QRTZ_* tables for durable scheduling.",
            e.message,
        )
        null
    }

    private fun startRam(reason: String): Scheduler {
        logger.info("Quartz using RAMJobStore ({})", reason)
        return createAndStart(
            commonProperties().apply {
                setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore")
            },
        )
    }

    private fun createAndStart(props: Properties): Scheduler {
        val s = StdSchedulerFactory(props).scheduler
        s.setJobFactory(KoinJobFactory())
        s.start()
        return s
    }

    inline fun <reified TJob : Job> scheduleRunOnceLaterJob(block: RunOnceLaterJobDsl<TJob>.() -> Unit) {
        RunOnceLaterJobDsl(TJob::class.java).apply(block).schedule(requireScheduler())
    }

    fun stats(): SchedulerStatGroup = try {
        if (!started.get()) {
            return SchedulerStatGroup.Unavailable(message = "scheduler not started")
        }
        val meta = scheduler.metaData
        SchedulerStatGroup.Ok(
            running = scheduler.isStarted && !scheduler.isShutdown,
            standby = scheduler.isInStandbyMode,
            jobStoreClass = meta.jobStoreClass.name,
            cluster = meta.isJobStoreClustered,
            threadPoolSize = meta.threadPoolSize,
            executingJobs = scheduler.currentlyExecutingJobs.size,
            scheduledJobs = scheduler.getJobKeys(GroupMatcher.anyJobGroup()).size,
            numberOfTriggers = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup()).size,
        )
    } catch (e: Exception) {
        SchedulerStatGroup.Unavailable(message = e.message ?: "scheduler unavailable")
    }

    fun shutdown() {
        if (started.get() && ::scheduler.isInitialized) {
            scheduler.shutdown(true)
        }
    }

    private fun commonProperties(): Properties = Properties().apply {
        setProperty("org.quartz.scheduler.instanceName", "LocusScheduler")
        setProperty("org.quartz.scheduler.instanceId", "AUTO")
        setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool")
        setProperty("org.quartz.threadPool.threadCount", "4")
        setProperty("org.quartz.threadPool.threadPriority", "5")
    }

    companion object {
        const val QUARTZ_DS_NAME = "locus"
    }
}

/** Wraps an existing [DataSource] for Quartz [DBConnectionManager]. */
private class DataSourceConnectionProvider(
    private val dataSource: DataSource,
) : ConnectionProvider {
    override fun getConnection(): Connection = dataSource.connection

    override fun shutdown() {
        // Pool lifecycle owned by DatabaseService
    }

    override fun initialize() {
        // no-op
    }
}
