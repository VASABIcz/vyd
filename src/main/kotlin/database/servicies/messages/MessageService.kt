package database.servicies.messages

import java.time.Instant

interface MessageService {
    fun createMessage(user: Int, channel: Int, content: String): Int?

    fun getMessage(id: Int): Message?

    fun editMessage(id: Int, content: String): Boolean

    fun deleteMessage(id: Int): Boolean

    fun getMessages(channel: Int, amount: Int = 0, offset: Int = 0, id: Int? = null): List<Message>

    fun getMessages(channel: Int, amount: Int, id: Int, olderThan: Boolean): List<DatabaseMessage>

    fun getMessages(channel: Int, amount: Int, timestamp: Instant, olderThan: Boolean): List<DatabaseMessage>
}