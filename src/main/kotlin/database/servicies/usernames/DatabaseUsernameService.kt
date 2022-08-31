package database.servicies.usernames

import base10to36
import base36to10
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf


class DatabaseUsernameService(private val database: Database): UsernameService {
    private val usernames get() = database.sequenceOf(DatabaseUsernames)
    override fun getDiscriminator(username: String): String? {
        val n = usernames.find {
            (DatabaseUsernames.username eq username)
        }
        return n?.discriminator
            ?: if (database.insert(DatabaseUsernames) {
                    set(DatabaseUsernames.username, username)
                    set(DatabaseUsernames.discriminator, "0")
                } == 0) {
                null
            } else {
                "0"
            }
    }

    override fun incrementDiscriminator(username: String): Boolean {
        val n = usernames.find {
            (it.username eq username)
        } ?: return false
        println("all guuuuuuuuuuuuuuuuuuuuuuuuud")
        return database.update(DatabaseUsernames) {
            set(DatabaseUsernames.discriminator, base10to36(base36to10(n.discriminator) + 1))
            where {
                DatabaseUsernames.username eq username
            }
        } != 0
    }
}