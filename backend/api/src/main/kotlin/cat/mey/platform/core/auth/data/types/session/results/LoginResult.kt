package cat.mey.platform.core.auth.data.types.session.results

import cat.mey.platform.core.auth.data.entities.Account

sealed interface LoginResult {
    data class Success(val account: Account) : LoginResult

    sealed interface Failure: LoginResult {
        object NotFound : Failure
        object InvalidPassword : Failure
    }
}