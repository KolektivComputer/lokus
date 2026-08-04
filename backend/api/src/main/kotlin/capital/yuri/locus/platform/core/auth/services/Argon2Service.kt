package capital.yuri.locus.platform.core.auth.services

import capital.yuri.locus.platform.core.auth.data.config.AuthConfig
import de.mkammerer.argon2.Argon2Factory
import org.koin.core.component.KoinComponent

class Argon2Service(private val authConfig: AuthConfig) : KoinComponent {
    private val argon2 = Argon2Factory.create(
        Argon2Factory.Argon2Types.ARGON2i,
        128,
        128,
    )

    fun hashPassword(password: CharArray): String = argon2.hash(authConfig.argon.saltRounds, 65536, 1, password)

    fun hashPassword(password: String) = hashPassword(password.toCharArray())

    fun verifyPassword(password: CharArray, hash: String) = argon2.verify(hash, password)

    fun verifyPassword(password: String, hash: String) = verifyPassword(password.toCharArray(), hash)
}
