package wrapers

import auth.hash.HashingService
import database.servicies.usernames.UsernameService
import database.servicies.users.UserService
import org.ktorm.database.Database

class UserWrapper(
    private val database: Database,
    private val userService: UserService,
    private val usernameService: UsernameService,
    private val hashingService: HashingService
) {
    fun createUser(username: String, password: String): Int? {
        val discriminator = usernameService.getDiscriminator(username) ?: return null
        val hash = hashingService.generateSaltedHash(password)
        return database.useTransaction {
            val id = userService.createUser(username, hash, discriminator) ?: throw Throwable("failed to create user")
            if (!usernameService.incrementDiscriminator(username)) {
                throw Throwable("failed to icnrement username")
            }
            id
        }
    }
}