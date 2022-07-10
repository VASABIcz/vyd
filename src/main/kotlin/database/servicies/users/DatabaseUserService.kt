package database.servicies.users

import auth.hash.SaltedHash
import io.ktor.server.auth.jwt.*
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class DatabaseUserService(private val database: Database) : UserService {
    private val users get() = database.sequenceOf(DatabaseUsers)

    override fun createUser(username: String, hash: SaltedHash, discriminator: String): Int? {
        return database.insertAndGenerateKey(DatabaseUsers) {
            set(DatabaseUsers.name, username)
            set(DatabaseUsers.discriminator, discriminator)
            set(DatabaseUsers.hash, hash.hash.toByteArray())
            set(DatabaseUsers.salt, hash.salt.toByteArray())
        } as Int?
    }

    override fun getUser(username: String, discriminator: String): DatabaseUser? {
        return users.find {
            (DatabaseUsers.name eq username) and (DatabaseUsers.discriminator eq discriminator)
        }
    }

    override fun getUser(id: Int): DatabaseUser? {
        return users.find {
            (DatabaseUsers.id eq id)
        }
    }

    override fun deleteUser(id: Int): Boolean {
        val user = getUser(id) ?: return false
        return user.delete() != 0
    }

    override fun deleteUser(username: String, discriminator: String): Boolean {
        val user = getUser(username, discriminator) ?: return false
        return user.delete() != 0
    }
}

fun JWTPrincipal.fetchUser(userService: UserService): User? {
    return this.getClaim("id", Int::class)?.let {
        userService.getUser(it)
    }
}

val JWTPrincipal.userId get() = this.getClaim("id", Int::class)