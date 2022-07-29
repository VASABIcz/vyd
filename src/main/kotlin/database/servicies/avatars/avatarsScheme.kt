package database.servicies.avatars

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.bytes
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import java.time.Instant


@kotlinx.serialization.Serializable
enum class AvatarOwner {
    user,
    guild
}

interface Image {
    val id: Int
    val timestamp: Instant
    val type: AvatarOwner
    val image: ByteArray
}

interface DatabaseGuildImage : Entity<DatabaseGuildImage>, Image {
    companion object : Entity.Factory<DatabaseGuildImage>()

    override val id: Int
    override val timestamp: Instant
    override val type: AvatarOwner
    override val image: ByteArray
}

interface DatabaseUserImage : Entity<DatabaseUserImage>, Image {
    companion object : Entity.Factory<DatabaseUserImage>()

    override val id: Int
    override val timestamp: Instant
    override val type: AvatarOwner
    override val image: ByteArray
}

interface DatabaseDefaultAvatar : Entity<DatabaseDefaultAvatar>, Image {
    companion object : Entity.Factory<DatabaseDefaultAvatar>()

    override val id: Int
    override val image: ByteArray
}

object DatabaseGuildImages : Table<DatabaseGuildImage>("guild_avatars") {
    val id = int("id").primaryKey().bindTo { it.id }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
    val image = bytes("avatar").bindTo { it.image }
}

object DatabaseUsersImages : Table<DatabaseUserImage>("user_avatars") {
    val id = int("user_id").primaryKey().bindTo { it.id }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
    val image = bytes("avatar").bindTo { it.image }
}

object DatabaseDefaultAvatars : Table<DatabaseDefaultAvatar>("default_avatars") {
    val id = int("id").primaryKey().bindTo { it.id }
    val image = bytes("avatar").bindTo { it.image }
}