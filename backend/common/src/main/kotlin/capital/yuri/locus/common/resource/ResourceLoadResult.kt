package capital.yuri.locus.common.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ResourceLoadResult<out T> {
    @Serializable
    @SerialName("ok")
    data class Ok<T>(val value: T, val path: String) : ResourceLoadResult<T>

    @Serializable
    @SerialName("not_found")
    data class NotFound(val path: String) : ResourceLoadResult<Nothing>

    @Serializable
    @SerialName("decode_error")
    data class DecodeError(val path: String, val message: String) : ResourceLoadResult<Nothing>
}
