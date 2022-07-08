package database.servicies.usernames

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface DatabaseUsername : Entity<DatabaseUsername> {
    companion object : Entity.Factory<DatabaseUsername>()

    val username: String
    val discriminator: String
}

object DatabaseUsernames : Table<DatabaseUsername>("usernames") {
    val username = varchar("username").primaryKey().bindTo { it.username }
    val discriminator = varchar("discriminator").bindTo { it.discriminator }
}