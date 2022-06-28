package database

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

    // 36
    // 36 36 36 36
    override fun getDiscriminator(username: String): String? {
        val n = usernames.find {
            (it.username eq username)
        }
        return n?.discriminator
            ?: if (database.insert(DatabaseUsernames) {
                    set(it.username, username)
                    set(it.discriminator, "0")
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
        return database.update(DatabaseUsernames) {
            set(it.discriminator, base10to36(base36to10(n.discriminator)+1))
            where {
                it.username eq username
            }
        } == 0
    }
}