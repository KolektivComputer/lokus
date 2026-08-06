package capital.yuri.locus.platform.core.stats.data.types

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class StatisticGroupId(val value: String) {
    companion object {
        val Instance = StatisticGroupId("instance")
        val Runtime = StatisticGroupId("runtime")
        val Database = StatisticGroupId("database")
        val Scheduler = StatisticGroupId("scheduler")
    }
}
