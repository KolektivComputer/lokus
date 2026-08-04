package capital.yuri.locus.platform.core.scheduling.services

import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.data.config.DatabaseConfig
import capital.yuri.locus.platform.core.scheduling.KoinJobFactory
import capital.yuri.locus.platform.core.scheduling.RunOnceLaterJobDsl
import capital.yuri.locus.platform.core.stats.data.types.groups.SchedulerStatGroup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.LoggerFactory
import java.util.Properties

/**
 * Quartz scheduler. Prefers JDBC (PostgreSQL) job store when DB credentials exist;
 * falls back to RAM if JDBC init fails (e.g. `QRTZ_*` tables not migrated yet).
 *
 * Apply Quartz `tables_postgres.sql` via migrate for durable jobs in production.
 */
class SchedulerService : KoinComponent {
    private val logger = LoggerFactory.getLogger(SchedulerService::class.java)
    private val configService by inject<ConfigService>()
    private val dbConfig by configService.config<DatabaseConfig>()

    val scheduler: Scheduler = startScheduler()

    private fun startScheduler(): Scheduler {
        val jdbcProps = jdbcPropertiesOrNull()
        if (jdbcProps != null) {
            try {
                return createAndStart(jdbcProps).also {
                    logger.info("Quartz using JDBC job store ({})", dbConfig.jdbcUrl)
                }
            } catch (e: Exception) {
                logger.warn(
                    "Quartz JDBC job store failed ({}). Falling back to RAM. " +
                        "Apply QRTZ_* tables for durable scheduling.",
                    e.message,
                )
            }
        }
        return createAndStart(ramProperties()).also {
            logger.info("Quartz using RAMJobStore")
        }
    }

    private fun createAndStart(props: Properties): Scheduler {
        val scheduler = StdSchedulerFactory(props).scheduler
        scheduler.setJobFactory(KoinJobFactory())
        scheduler.start()
        return scheduler
    }

    inline fun <reified TJob : Job> scheduleRunOnceLaterJob(block: RunOnceLaterJobDsl<TJob>.() -> Unit) {
        RunOnceLaterJobDsl(TJob::class.java).apply(block).schedule(scheduler)
    }

    fun stats(): SchedulerStatGroup = try {
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

    private fun jdbcPropertiesOrNull(): Properties? {
        val user = dbConfig.username ?: return null
        if (user.isBlank()) return null
        return Properties().apply {
            putAll(commonProperties())
            setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX")
            setProperty(
                "org.quartz.jobStore.driverDelegateClass",
                "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
            )
            setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_")
            setProperty("org.quartz.jobStore.dataSource", "locus")
            setProperty("org.quartz.jobStore.isClustered", "false")
            setProperty("org.quartz.dataSource.locus.driver", dbConfig.driver.driver)
            setProperty("org.quartz.dataSource.locus.URL", dbConfig.jdbcUrl)
            setProperty("org.quartz.dataSource.locus.user", user)
            setProperty("org.quartz.dataSource.locus.password", dbConfig.password ?: "")
            setProperty("org.quartz.dataSource.locus.maxConnections", "5")
        }
    }

    private fun ramProperties(): Properties = Properties().apply {
        putAll(commonProperties())
        setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore")
    }

    private fun commonProperties(): Properties = Properties().apply {
        setProperty("org.quartz.scheduler.instanceName", "LocusScheduler")
        setProperty("org.quartz.scheduler.instanceId", "AUTO")
        setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool")
        setProperty("org.quartz.threadPool.threadCount", "4")
        setProperty("org.quartz.threadPool.threadPriority", "5")
    }
}
