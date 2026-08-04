package capital.yuri.locus.platform.core.auth.data.types.session.results

import capital.yuri.locus.platform.core.auth.data.entities.Account

sealed interface LoginResult {
    data class Success(val account: Account) : LoginResult

    sealed interface Failure : LoginResult {
        object NotFound : Failure
        object InvalidPassword : Failure
    }
}
