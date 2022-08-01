package wrapers

import database.servicies.usernames.UsernameService
import database.servicies.users.UserService
import org.ktorm.database.Database
import utils.hash.HashingService

class UserWrapper(
    private val database: Database,
    private val userService: UserService,
    private val usernameService: UsernameService,
    private val hashingService: HashingService
) {
    private fun isValidPassword(password: String): Boolean {
        // FIXME
        return !(password.length < 4 || password.isBlank())
    }

    private fun isValidUsername(username: String): Boolean {
        // FIXME
        return !(username.isBlank() || username.contains(" "))
    }

    suspend fun createUser(username: String, password: String): Int? {
        if (!isValidPassword(password) || !isValidUsername(username)) {
            return null
        }

        val discriminator = usernameService.getDiscriminator(username) ?: return null
        val hash = hashingService.generateSaltedHash(password)
        return database.useTransaction {
            val id = userService.createUser(username, hash, discriminator) ?: throw Throwable("failed to create user")
            if (!usernameService.incrementDiscriminator(username)) {
                throw Throwable("failed to increment username")
            }
            id
        }
    }
}