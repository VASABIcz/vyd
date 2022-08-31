package database.servicies.messages

import data.responses.MessagesMessage
import database.servicies.channels.Channel
import database.servicies.channels.DatabaseChannel
import database.servicies.channels.DatabaseChannels
import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import database.servicies.users.User
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text
import org.ktorm.schema.timestamp
import java.time.Instant

interface Message {
    val id: Int
    val content: String
    val author: User
    val timestamp: Instant
    val channel: Channel

    fun toMessagesMessage(): MessagesMessage {
        return MessagesMessage(id, author.toUsersUser(), content, timestamp.toEpochMilli(), channel.id)
    }
}

interface DatabaseMessage : Entity<DatabaseMessage>, Message {
    companion object : Entity.Factory<DatabaseMessage>()

    override val id: Int
    override var content: String
    override val author: DatabaseUser
    override val timestamp: Instant
    override val channel: DatabaseChannel
}

object DatabaseMessages : Table<DatabaseMessage>("messages") {
    val id = int("id").primaryKey().bindTo { it.id }
    val timestamp = timestamp("timestamp").bindTo { it.timestamp }
    val content = text("content").bindTo { it.content }
    val author = int("user_id").references(DatabaseUsers) { it.author }
    val channel = int("channel_id").references(DatabaseChannels) { it.channel }
}