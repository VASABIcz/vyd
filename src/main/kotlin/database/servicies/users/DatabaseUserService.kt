package database.servicies.users

import io.ktor.server.auth.jwt.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import utils.hash.SaltedHash

class DatabaseUserService(private val database: Database) : UserService {
    private val users get() = database.sequenceOf(DatabaseUsers)

    override suspend fun createUser(username: String, hash: SaltedHash, discriminator: String): Int? =
        withContext(Dispatchers.IO) {
            return@withContext database.insertAndGenerateKey(DatabaseUsers) {
                set(DatabaseUsers.name, username)
                set(DatabaseUsers.discriminator, discriminator)
                set(DatabaseUsers.hash, hash.hash.toByteArray())
                set(DatabaseUsers.salt, hash.salt.toByteArray())
            } as Int?
        }

    override suspend fun getUser(username: String, discriminator: String): DatabaseUser? = withContext(Dispatchers.IO) {
        return@withContext users.find {
            (DatabaseUsers.name eq username) and (DatabaseUsers.discriminator eq discriminator)
        }
    }

    override suspend fun getUser(id: Int): DatabaseUser? = withContext(Dispatchers.IO) {
        return@withContext users.find {
            (DatabaseUsers.id eq id)
        }
    }

    override suspend fun deleteUser(id: Int): Boolean = withContext(Dispatchers.IO) {
        val user = getUser(id) ?: return@withContext false
        return@withContext user.delete() != 0
    }
}

suspend fun JWTPrincipal.fetchUser(userService: UserService): User? {
    return this.getClaim("id", Int::class)?.let {
        userService.getUser(it)
    }
}

val JWTPrincipal.userId get() = this.getClaim("id", Int::class)