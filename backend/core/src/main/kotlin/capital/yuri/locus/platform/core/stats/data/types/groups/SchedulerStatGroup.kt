package capital.yuri.locus.platform.core.stats.data.types.groups

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface SchedulerStatGroup {
    @Serializable
    @SerialName("ok")
    data class Ok(
        val running: Boolean,
        val standby: Boolean,
        val jobStoreClass: String,
        val cluster: Boolean,
        val threadPoolSize: Int,
        val executingJobs: Int,
        val scheduledJobs: Int,
        val numberOfTriggers: Int,
    ) : SchedulerStatGroup

    @Serializable
    @SerialName("unavailable")
    data class Unavailable(
        val message: String,
    ) : SchedulerStatGroup
}
