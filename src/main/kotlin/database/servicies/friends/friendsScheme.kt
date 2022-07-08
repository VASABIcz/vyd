package database.servicies.friends

import data.responses.FriendsFriend
import database.servicies.channels.DatabaseChannel
import database.servicies.channels.DatabaseChannels
import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int

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