package database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

enum class ChannelType {
    friends,
    text,
    voice,
    group,
    category
}

interface DatabaseUser : Entity<DatabaseUser> {
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

object DatabaseUsernames : Table<DatabaseUsername>("usernames") {
    val username = varchar("username").primaryKey().bindTo { it.username }
    val discriminator = varchar("discriminator").bindTo { it.discriminator }
}

interface DatabaseFriend : Entity<DatabaseFriend> {
    companion object : Entity.Factory<DatabaseFriend>()

    val user1: DatabaseUser
    val user2: DatabaseUser
    val channel: DatabaseChannel
    var areFriends: Boolean
}

object DatabaseFriends : Table<DatabaseFriend>("friends") {
    val user1 = int("user1").references(DatabaseUsers) { it.user1 }
    val user2 = int("user2").references(DatabaseUsers) { it.user2 }
    val channel = int("channel_id").references(DatabaseChannels) { it.channel }
    val friends = boolean("friends").bindTo { it.areFriends }
}

interface DatabaseChannel : Entity<DatabaseChannel> {
    companion object : Entity.Factory<DatabaseChannel>()

    val id: Int
    val timestamp: Instant
    val type: ChannelType
}

object DatabaseChannels : Table<DatabaseChannel>("channels") {
    val id = int("id").primaryKey().bindTo { it.id }
    val timestamp = timestamp("timestamp"/*FIXME*/).bindTo { it.timestamp }
    val type = enum<ChannelType>("type").bindTo { it.type }
}

interface DatabaseMessage : Entity<DatabaseMessage> {
    companion object : Entity.Factory<DatabaseMessage>()

    val id: Int
    val content: String
    val author: DatabaseUser
    val timestamp: Instant
    val channel: DatabaseChannel

}

object DatabaseMessages : Table<DatabaseMessage>("messages") {
    val id = int("id").primaryKey().bindTo { it.id }
    val timestamp = timestamp("timestamp"/*FIXME*/).bindTo { it.timestamp }
    val content = text("content").bindTo { it.content }
    val author = int("user_id").references(DatabaseUsers) { it.author }
    val channel = int("channel_id").references(DatabaseChannels) { it.channel }
}