package database

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseFriendService(val database: Database) : FriendService {
    // TODO friend requests
    // user1 accepted
    // user2 accepted
    // or create friend_settings table and assume friends false by default

    private val friends get() = database.sequenceOf(DatabaseFriends)
    private val messages get() = database.sequenceOf(DatabaseMessages)

    override fun addFriend(user1: DatabaseUser, user2: DatabaseUser): Boolean {
        val res = database.useTransaction {
            val channel = database.insertAndGenerateKey(DatabaseChannels) {
                set(it.type, ChannelType.friends)
            }

            val f = database.insert(DatabaseFriends) {
                set(it.user1, user1.id)
                set(it.user2, user2.id)
                set(it.channel, channel as Int)
            }
            f != 0
        }

        return res
    }

    override fun removeFriend(user1: DatabaseUser, user2: DatabaseUser): Boolean {
        val x = getFriendship(user1, user2) ?: return false
        x.areFriends = false
        return x.flushChanges() != 0
    }

    override fun getFriendship(user1: DatabaseUser, user2: DatabaseUser): DatabaseFriend? {
        return friends.find {
            ((it.user1 eq user1.id) or (it.user1 eq user2.id)) and ((it.user2 eq user1.id) or (it.user2 eq user2.id))
        }
    }

    override fun getFriends(user: DatabaseUser): List<DatabaseFriend> {
        return friends.filter {
            ((it.user1 eq user.id) or (it.user2 eq user.id)) // FIXME and it.friends not sure
            // if they are not friends anymore they should still see message history but shouldn't be allowed to send message so probably dont check
        }.toList()
    }

    override fun sendMessage(sender: DatabaseUser, receiver: DatabaseUser, content: String): Boolean {
        val f = getFriendship(sender, receiver) ?: return false
        if (!f.areFriends) { // FIXME not sure if it should be handled here
            return false
        }

        val m = database.insert(DatabaseMessages) {
            set(it.author, sender.id)
            set(it.channel, f.channel.id)
            set(it.content, content)
        }

        return m != 0
    }

    override fun getMessages(
        user1: DatabaseUser,
        user2: DatabaseUser,
        amount: Int,
        offset: Int
    ): List<DatabaseMessage>? {
        val f = getFriendship(user1, user2) ?: return null
        /* FIXME
        val messages = mutableListOf<DatabaseMessage>()
        val q = database
            .from(DatabaseMessages).select().offset(offset).limit(amount).where {
                DatabaseMessages.channel eq f.channel.id
            }.map {
                DatabaseMessage()
            }

        database.executeQuery(q)

         */
        return messages.filter {
            it.channel eq f.channel.id
        }.toList()
    }
}
