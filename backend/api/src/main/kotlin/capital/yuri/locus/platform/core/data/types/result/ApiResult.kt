package capital.yuri.locus.platform.core.data.types.result

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

interface ApiResult {
    val statusCode: HttpStatusCode

    val success: Boolean

    @Serializable
    abstract class Success : ApiResult {
        @Transient
        override val statusCode: HttpStatusCode = HttpStatusCode.OK

        override val success = true
    }

    @Serializable
    abstract class Error : ApiResult {
        override val success = false

        abstract val translationKey: String
        val translationValues = mutableMapOf<String, String>()
    }
}
