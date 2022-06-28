package database

import auth.hash.SaltedHash
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.time.Instant

class DatabaseUserService(private val database: Database, private val usernameService: UsernameService): UserService {
    private val users get() = database.sequenceOf(DatabaseUsers)

    override fun createUser(username: String, hash: SaltedHash): Boolean {
        val res = database.useTransaction {
            val discriminator  = usernameService.getDiscriminator(username)
            database.insert(DatabaseUsers) {
                set(it.name, username)
                set(it.discriminator, discriminator!!)
                set(it.registerDate, Instant.now())
                set(it.hash, hash.hash.toByteArray())
                set(it.salt, hash.salt.toByteArray())
            }
            if (usernameService.incrementDiscriminator(username)) {
                throw Throwable("failed to increment discriminator")
            }
            true
        }
        return res
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