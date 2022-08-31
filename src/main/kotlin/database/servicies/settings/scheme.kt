package database.servicies.settings

import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import database.servicies.users.User
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text
import org.ktorm.schema.varchar

// blocks
// accepts_dms

interface UserSetting {
    val user: User
    val key: String
    val value: String
}

interface DatabaseUserSetting : Entity<DatabaseUserSetting>, UserSetting {
    companion object : Entity.Factory<DatabaseUserSetting>()

    override val user: DatabaseUser
    override val key: String
    override val value: String
}

object DatabaseUserSettings : Table<DatabaseUserSetting>("user_settings") {
    val user = int("user_id").references(DatabaseUsers) { it.user }
    val key = varchar("key").bindTo { it.key }
    val value = text("value").bindTo { it.value }
}