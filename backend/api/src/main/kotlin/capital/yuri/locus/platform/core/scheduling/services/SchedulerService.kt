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
import org.slf4j.LoggerFactory
import java.util.Properties

/**
 * Quartz scheduler backed by JDBC (PostgreSQL) when database config is available,
 * otherwise falls back to RAM store (dev / pre-connect).
 *
 * JDBC store requires Quartz tables (`QRTZ_*`). Apply the Postgres script from the
 * Quartz distribution (`tables_postgres.sql`) via migrate when you enable this in prod.
 */
class SchedulerService : KoinComponent {
    private val logger = LoggerFactory.getLogger(SchedulerService::class.java)
    private val configService by inject<ConfigService>()
    private val dbConfig by configService.config<DatabaseConfig>()

    val scheduler: Scheduler

    init {
        val props = buildProperties()
        val factory = StdSchedulerFactory(props)
        scheduler = factory.scheduler
        scheduler.setJobFactory(KoinJobFactory())
        scheduler.start()
        logger.info(
            "Quartz started jobStore={}",
            props.getProperty("org.quartz.jobStore.class"),
        )
    }

    inline fun <reified TJob : Job> scheduleRunOnceLaterJob(block: RunOnceLaterJobDsl<TJob>.() -> Unit) {
        RunOnceLaterJobDsl(TJob::class.java).apply(block).schedule(scheduler)
    }

    fun stats(): SchedulerStatGroup = try {
        val meta = scheduler.metaData
        val executing = scheduler.currentlyExecutingJobs.size
        val jobKeys = scheduler.getJobKeys(org.quartz.impl.matchers.GroupMatcher.anyJobGroup())
        val triggerKeys = scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.anyTriggerGroup())
        SchedulerStatGroup.Ok(
            running = scheduler.isStarted && !scheduler.isShutdown,
            standby = scheduler.isInStandbyMode,
            jobStoreClass = meta.jobStoreClass.name,
            cluster = meta.isJobStoreClustered,
            threadPoolSize = meta.threadPoolSize,
            executingJobs = executing,
            scheduledJobs = jobKeys.size,
            numberOfTriggers = triggerKeys.size,
        )
    } catch (e: Exception) {
        SchedulerStatGroup.Unavailable(message = e.message ?: "scheduler unavailable")
    }

    private fun buildProperties(): Properties = Properties().apply {
        setProperty("org.quartz.scheduler.instanceName", "LocusScheduler")
        setProperty("org.quartz.scheduler.instanceId", "AUTO")
        setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool")
        setProperty("org.quartz.threadPool.threadCount", "4")
        setProperty("org.quartz.threadPool.threadPriority", "5")

        // Prefer JDBC store when we have credentials; tables must exist.
        val user = dbConfig.username
        val pass = dbConfig.password
        if (!user.isNullOrBlank()) {
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
            setProperty("org.quartz.dataSource.locus.password", pass ?: "")
            setProperty("org.quartz.dataSource.locus.maxConnections", "5")
        } else {
            logger.warn("No DB user configured — Quartz using RAMJobStore")
            setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore")
        }
    }
}
