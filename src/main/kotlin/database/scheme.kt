package database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

interface DatabaseUser: Entity<DatabaseUser> {
    companion object : Entity.Factory<DatabaseUser>()
    val id: Int
    val name: String
    val hash: ByteArray
    val discriminator: String
    val registerDate: Instant
    val salt: ByteArray
}

interface DatabaseToken : Entity<DatabaseToken> {
    companion object : Entity.Factory<DatabaseToken>()
    val id: Int
    val tokenHash: ByteArray
    val user: DatabaseUser
}

interface DatabaseUsername : Entity<DatabaseUsername> {
    companion object : Entity.Factory<DatabaseUsername>()
    val username: String
    val discriminator: String
}

object DatabaseTokens : Table<DatabaseToken>("tokens") {
    val id = int("id").primaryKey().bindTo { it.id }
    val tokenHash = bytes("token_hash").bindTo { it.tokenHash }
    val user = int("user_id").references(DatabaseUsers) { it.user }
}

object DatabaseUsers : Table<DatabaseUser>("users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val discriminator = varchar("discriminator").bindTo { it.discriminator }
    val registerDate = timestamp("register_date").bindTo { it.registerDate }
    val hash = bytes("hash").bindTo { it.hash }
    val salt = bytes("salt").bindTo { it.salt }
}

object DatabaseUsernames: Table<DatabaseUsername>("usernames") {
    val username = varchar("username").primaryKey().bindTo { it.username }
    val discriminator = varchar("discriminator").bindTo { it.discriminator }
}