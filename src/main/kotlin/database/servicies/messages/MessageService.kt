package database.servicies.messages

interface MessageService {
    fun createMessage(user: Int, channel: Int, content: String): Int?

    fun getMessage(id: Int): Message?

    fun editMessage(id: Int, content: String): Boolean

    fun deleteMessage(id: Int): Boolean

    fun getMessages(channel: Int, amount: Int = 0, offset: Int = 0, id: Int? = null): List<Message>
}