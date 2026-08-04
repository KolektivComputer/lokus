package capital.yuri.locus.platform.core.stats

import capital.yuri.locus.platform.core.stats.data.types.StatisticGroupId
import capital.yuri.locus.platform.core.stats.data.types.results.GetStatGroupResult

/**
 * SPI for core + extension statistic groups.
 * Extensions register via [StatsService.register].
 */
fun interface StatisticGroupProvider {
    val id: StatisticGroupId

    suspend fun collect(): GetStatGroupResult
}
