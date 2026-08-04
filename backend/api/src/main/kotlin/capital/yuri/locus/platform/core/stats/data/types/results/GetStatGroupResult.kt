package capital.yuri.locus.platform.core.stats.data.types.results

import capital.yuri.locus.platform.core.stats.data.types.StatisticGroupId
import capital.yuri.locus.platform.core.stats.data.types.groups.DatabaseStatGroup
import capital.yuri.locus.platform.core.stats.data.types.groups.InstanceStatGroup
import capital.yuri.locus.platform.core.stats.data.types.groups.RuntimeStatGroup
import capital.yuri.locus.platform.core.stats.data.types.groups.SchedulerStatGroup
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Result of fetching a single statistic group.
 * Core groups use typed payloads; extension groups use [Extension] with free-form JSON.
 */
@Serializable
sealed interface GetStatGroupResult {
    val groupId: StatisticGroupId

    @Serializable
    @SerialName("success_instance")
    data class SuccessInstance(
        override val groupId: StatisticGroupId = StatisticGroupId.Instance,
        val data: InstanceStatGroup,
    ) : GetStatGroupResult

    @Serializable
    @SerialName("success_runtime")
    data class SuccessRuntime(
        override val groupId: StatisticGroupId = StatisticGroupId.Runtime,
        val data: RuntimeStatGroup,
    ) : GetStatGroupResult

    @Serializable
    @SerialName("success_database")
    data class SuccessDatabase(
        override val groupId: StatisticGroupId = StatisticGroupId.Database,
        val data: DatabaseStatGroup,
    ) : GetStatGroupResult

    @Serializable
    @SerialName("success_scheduler")
    data class SuccessScheduler(
        override val groupId: StatisticGroupId = StatisticGroupId.Scheduler,
        val data: SchedulerStatGroup,
    ) : GetStatGroupResult

    /** Extension-provided group — payload shape is owned by the extension. */
    @Serializable
    @SerialName("success_extension")
    data class SuccessExtension(
        override val groupId: StatisticGroupId,
        val data: JsonElement,
    ) : GetStatGroupResult

    @Serializable
    @SerialName("not_found")
    data class NotFound(
        override val groupId: StatisticGroupId,
    ) : GetStatGroupResult

    @Serializable
    @SerialName("failed")
    data class Failed(
        override val groupId: StatisticGroupId,
        val message: String,
    ) : GetStatGroupResult
}
