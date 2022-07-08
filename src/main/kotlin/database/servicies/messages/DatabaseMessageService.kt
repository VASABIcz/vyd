package database.servicies.messages

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

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

    override fun getMessages(channel: Int, amount: Int, offset: Int): List<DatabaseMessage> {
        return messages.filter {
            it.channel eq channel
        }.toList()
    }


}