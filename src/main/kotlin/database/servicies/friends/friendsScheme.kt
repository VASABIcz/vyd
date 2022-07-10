package database.servicies.friends

import data.responses.FriendsFriend
import database.servicies.channels.Channel
import database.servicies.channels.DatabaseChannel
import database.servicies.channels.DatabaseChannels
import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import database.servicies.users.User
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int

interface Friend {
    val user1: User
    val user2: User
    val channel: Channel
    var areFriends: Boolean

    fun toFriendsFriend(me: Int): FriendsFriend
}

interface DatabaseFriend : Entity<DatabaseFriend>, Friend {
    companion object : Entity.Factory<DatabaseFriend>()

    override val user1: DatabaseUser
    override val user2: DatabaseUser
    override val channel: DatabaseChannel
    override var areFriends: Boolean

    override fun toFriendsFriend(me: Int): FriendsFriend {
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