package database.servicies.users

import data.responses.UsersUser
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

interface DatabaseUser : Entity<DatabaseUser> {
    companion object : Entity.Factory<DatabaseUser>()

    val id: Int
    val name: String
    val hash: ByteArray
    val discriminator: String
    val registerDate: Instant
    val salt: ByteArray

    fun toUsersUser(): UsersUser {
        return UsersUser(id, name, discriminator)
    }
}

object DatabaseUsers : Table<DatabaseUser>("users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val discriminator = varchar("discriminator").bindTo { it.discriminator }
    val registerDate = timestamp("register_date").bindTo { it.registerDate }
    val hash = bytes("hash").bindTo { it.hash }
    val salt = bytes("salt").bindTo { it.salt }
}