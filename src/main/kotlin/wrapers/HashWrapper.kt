package wrapers

import auth.hash.HashingService
import auth.hash.SaltedHash
import auth.token.TokenClaim
import auth.token.TokenConfig
import auth.token.TokenService
import database.servicies.users.UserService

class HashWrapper(
    private val hashingService: HashingService,
    private val userService: UserService,
    private val tokenService: TokenService,
    private val config: TokenConfig
) {
    fun createToken(username: String, discriminator: String, password: String): String? {
        val user = userService.getUser(username, discriminator) ?: return null
        return if (hashingService.verify(
                password,
                SaltedHash(user.hash.decodeToString(), user.salt.decodeToString())
            )
        ) {
            tokenService.generate(config, TokenClaim("id", user.id.toString()))
        } else {
            null
        }
    }

    fun createToken(id: Int, password: String): String? {
        val user = userService.getUser(id) ?: return null
        return if (hashingService.verify(
                password,
                SaltedHash(user.hash.decodeToString(), user.salt.decodeToString())
            )
        ) {
            tokenService.generate(config, TokenClaim("id", user.id.toString()))
        } else {
            null
        }
    }
}