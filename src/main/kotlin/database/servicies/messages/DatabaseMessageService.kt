package database.servicies.messages

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.Instant

class DatabaseMessageService(private val database: Database) : MessageService {
    private val messages get() = database.sequenceOf(DatabaseMessages)

    override fun createMessage(user: Int, channel: Int, content: String): Int? {
        return database.insertAndGenerateKey(DatabaseMessages) {
            set(it.author, user)
            set(it.channel, channel)
            set(it.content, content)
        } as Int?
    }

    override fun getMessage(id: Int): DatabaseMessage? {
        return messages.find {
            it.id eq id
        }
    }

    override fun editMessage(id: Int, content: String): Boolean {
        val message = getMessage(id) ?: return false
        message.content = content
        return message.flushChanges() > 0
    }

    override fun deleteMessage(id: Int): Boolean {
        val message = getMessage(id) ?: return false
        return message.delete() > 0
    }

    override fun getMessages(channel: Int, amount: Int, offset: Int, id: Int?): List<DatabaseMessage> {
        return if (id != null) {
            messages.filter {
                (it.channel eq channel) and (it.id gt id)
            }.take(amount).toList()
        } else {
            messages.filter {
                it.channel eq channel
            }.drop(offset).take(amount).toList()
        }
    }

    override fun getMessages(channel: Int, amount: Int, id: Int, olderThan: Boolean): List<DatabaseMessage> {
        return if (olderThan) {
            messages.filter {
                (it.channel eq channel) and (it.id less id)
            }.take(amount).toList()
        } else {
            messages.filter {
                (it.channel eq channel) and (it.id gt id)
            }.take(amount).toList()
        }
    }

    override fun getMessages(channel: Int, amount: Int, timestamp: Instant, olderThan: Boolean): List<DatabaseMessage> {
        return if (olderThan) {
            messages.filter {
                (it.channel eq channel) and (it.timestamp less timestamp)
            }.take(amount).toList()
        } else {
            messages.filter {
                (it.channel eq channel) and (it.timestamp gt timestamp)
            }.take(amount).toList()
        }
    }
}