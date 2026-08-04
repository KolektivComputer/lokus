package capital.yuri.locus.platform.core.config.data.types.results

internal sealed interface ConfigLoadResult {
    data class Loaded<T>(val data: T) : ConfigLoadResult

    enum class Failure : ConfigLoadResult {
        NotFound,
        DecodeError,
    }
}
