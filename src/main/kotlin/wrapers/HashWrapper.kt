package wrapers

import database.servicies.users.UserService
import utils.hash.HashingService
import utils.hash.SaltedHash
import utils.token.TokenClaim
import utils.token.TokenConfig
import utils.token.TokenService

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