@file:OptIn(ExperimentalUuidApi::class)

package cat.mey.platform.core.auth.services

import cat.mey.platform.core.auth.data.entities.Account
import cat.mey.platform.core.auth.data.entities.PasswordResetRequest
import cat.mey.platform.core.auth.data.types.results.ResetPasswordResult
import cat.mey.platform.core.auth.data.types.session.results.LoginResult
import cat.mey.platform.core.auth.jobs.ExpirePasswordResetRequestJob
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue
import kotlin.uuid.ExperimentalUuidApi

class AccountAuthenticationService : KoinComponent {
    private val argon2 by inject<Argon2Service>()

    // region login

    fun login(usernameOrEmail: String, password: String): LoginResult {
        val account = transaction { Account.findByUsernameOrEmail(usernameOrEmail) }
        if (account == null) return LoginResult.Failure.NotFound

        val passwordMatches = argon2.verifyPassword(password, account.passwordHash)

        return if (passwordMatches) LoginResult.Success(account)
        else LoginResult.Failure.InvalidPassword
    }

    // endregion login

    // region password reset

    suspend fun requestPasswordResetSuspend(account: Account) {
        val newRequest = suspendTransaction { PasswordResetRequest.create(account, generateToken()) }
        ExpirePasswordResetRequestJob.dispatch(newRequest.id.value)
        // todo: send link to email
    }

    private fun generateToken(): String {
        TODO()
    }

    fun resetPassword(password: String, token: String): ResetPasswordResult {
        val request = transaction { PasswordResetRequest.findByToken(token) }
            ?: return ResetPasswordResult.Failure.RequestNotFound

        request.expired?.let {
            return ResetPasswordResult.Failure.TokenExpired(it)
        }

        val newHash = argon2.hashPassword(password)

        transaction {
            request.account.passwordHash = newHash
        }

        return ResetPasswordResult.Success
    }

    // endregion password reset
}