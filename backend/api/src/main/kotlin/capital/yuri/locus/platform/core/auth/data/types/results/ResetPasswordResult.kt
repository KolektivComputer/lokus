package capital.yuri.locus.platform.core.auth.data.types.results

import kotlin.time.Instant

sealed interface ResetPasswordResult {
    object Success : ResetPasswordResult
    sealed interface Failure : ResetPasswordResult {
        data class TokenExpired(val timestamp: Instant) : Failure
        object RequestNotFound : Failure
    }
}
