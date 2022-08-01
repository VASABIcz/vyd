package database.servicies.friends

import database.servicies.channels.ChannelType
import database.servicies.channels.DatabaseChannels
import database.servicies.messages.DatabaseMessage
import database.servicies.messages.DatabaseMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*

class DatabaseFriendService(val database: Database) : FriendService {
    private val friends get() = database.sequenceOf(DatabaseFriends)
    private val messages get() = database.sequenceOf(DatabaseMessages)

    override suspend fun addFriend(user1: Int, user2: Int): Boolean = withContext(Dispatchers.IO) {
        val f = getFriendship(user1, user2)
        return@withContext if (f == null) {
            database.useTransaction {
                val channel = database.insertAndGenerateKey(DatabaseChannels) {
                    set(DatabaseChannels.type, ChannelType.friends)
                }

                val f = database.insert(DatabaseFriends) {
                    set(DatabaseFriends.user1, user1)
                    set(DatabaseFriends.user2, user2)
                    set(DatabaseFriends.channel, channel as Int)
                }
                f != 0
            }
        } else {
            f.areFriends = true
            f.flushChanges() != 0
        }
    }

    override suspend fun removeFriend(user1: Int, user2: Int): Boolean = withContext(Dispatchers.IO) {
        val x = getFriendship(user1, user2) ?: return@withContext false
        x.areFriends = false
        return@withContext x.flushChanges() != 0
    }

    override suspend fun getFriendship(user1: Int, user2: Int): DatabaseFriend? = withContext(Dispatchers.IO) {
        return@withContext friends.find {
            ((DatabaseFriends.user1 eq user1) or (DatabaseFriends.user1 eq user2)) and ((DatabaseFriends.user2 eq user1) or (DatabaseFriends.user2 eq user2))
        }
    }

    override suspend fun getFriends(user: Int): Set<DatabaseFriend> = withContext(Dispatchers.IO) {
        return@withContext friends.filter {
            ((DatabaseFriends.user1 eq user) or (DatabaseFriends.user2 eq user)) and DatabaseFriends.friends eq true  // FIXME and it.friends not sure
            // if they are not friends anymore they should still see message history but shouldn't be allowed to send message so probably dont check
        }.toSet()
    }

    override suspend fun sendMessage(sender: Int, receiver: Int, content: String): Int? = withContext(Dispatchers.IO) {
        val f = getFriendship(sender, receiver) ?: return@withContext null
        if (!f.areFriends) { // FIXME not sure if it should be handled here
            return@withContext null
        }

        return@withContext database.insertAndGenerateKey(DatabaseMessages) {
            set(DatabaseMessages.author, sender)
            set(DatabaseMessages.channel, f.channel.id)
            set(DatabaseMessages.content, content)
        } as Int?
    }

    override suspend fun getMessages(
        user1: Int,
        user2: Int,
        amount: Int,
        offset: Int
    ): List<DatabaseMessage>? = withContext(Dispatchers.IO) {
        val f = getFriendship(user1, user2) ?: return@withContext null
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
        return@withContext messages.filter {
            DatabaseMessages.channel eq f.channel.id
        }.toList()
    }
}
