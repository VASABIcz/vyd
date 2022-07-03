package database

import data.responses.FriendRequestsRequest
import data.responses.FriendsFriend
import data.responses.MessagesMessage
import data.responses.UsersUser
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

enum class ChannelType {
    friends, text, voice, group, category
}

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

    fun toFriendsFriend(me: Int): FriendsFriend {
        return FriendsFriend(
            if (user1.id != me) user1.toUsersUser() else user2.toUsersUser(),
            channel.id,
            channel.timestamp.toEpochMilli(),
            areFriends
        )
    }
}

object DatabaseFriends : Table<DatabaseFriend>("friends") {
    val user1 = int("user1").references(DatabaseUsers) { it.user1 }
    val user2 = int("user2").references(DatabaseUsers) { it.user2 }
    val channel = int("channel_id").primaryKey().references(DatabaseChannels) { it.channel }
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
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
    val type = enum<ChannelType>("type").bindTo { it.type }
}

interface DatabaseMessage : Entity<DatabaseMessage> {
    companion object : Entity.Factory<DatabaseMessage>()

    val id: Int
    val content: String
    val author: DatabaseUser
    val timestamp: Instant
    val channel: DatabaseChannel

    fun toMessagesMessage(): MessagesMessage {
        return MessagesMessage(id, author.toUsersUser(), content, timestamp.toEpochMilli(), channel.id)
    }
}

object DatabaseMessages : Table<DatabaseMessage>("messages") {
    val id = int("id").primaryKey().bindTo { it.id }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
    val content = text("content").bindTo { it.content }
    val author = int("user_id").references(DatabaseUsers) { it.author }
    val channel = int("channel_id").references(DatabaseChannels) { it.channel }
}

/*

interface UsersAvatar: Entity<UsersAvatar> {
    companion object : Entity.Factory<UsersAvatar>()

    val user: DatabaseUser
    val avatar: ByteArray
    val timestamp: Instant
}
object DatabaseUsersAvatars : Table<UsersAvatar>("users_avatars") {
    val id = int("id").primaryKey().references(DatabaseUsers) {it.user}
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
    val content = bytes("avatar").bindTo { it.avatar }
}

 */
@kotlinx.serialization.Serializable
enum class FriendRequestState {
    accepted,
    pending,
    canceled
}

@kotlinx.serialization.Serializable
enum class FriendRequestResponse {
    accept,
    decline
}

fun FriendRequestResponse.toFriendRequestState(): FriendRequestState {
    return when (this) {
        FriendRequestResponse.accept -> FriendRequestState.accepted
        FriendRequestResponse.decline -> FriendRequestState.canceled
    }
}

interface DatabaseFriendRequest : Entity<DatabaseFriendRequest> {
    companion object : Entity.Factory<DatabaseFriendRequest>()

    val id: Int
    val requester: DatabaseUser
    val receiver: DatabaseUser
    var state: FriendRequestState
    val timestamp: Instant

    fun toFriendRequestsRequest(): FriendRequestsRequest {
        return FriendRequestsRequest(id, requester.toUsersUser(), timestamp.toEpochMilli())
    }
}

object DatabaseFriendRequests : Table<DatabaseFriendRequest>("friend_requests") {
    val id = int("id").primaryKey().bindTo { it.id }
    val requester = int("requester").references(DatabaseUsers) { it.requester }
    val receiver = int("receiver").references(DatabaseUsers) { it.receiver }
    val state = enum<FriendRequestState>("state").bindTo { it.state }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
}
