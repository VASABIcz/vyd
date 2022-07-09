package wrapers

import data.responses.MessagesMessage
import database.servicies.friends.FriendService
import database.servicies.messages.MessageService

class FriendWrapper(
    private val friendService: FriendService,
    private val messageService: MessageService
) {
    fun getMessages(userId: Int, friendId: Int): List<MessagesMessage>? {
        val friends = friendService.getFriendship(userId, friendId) ?: return null

        val messages = messageService.getMessages(friends.channel.id)

        return messages.map {
            it.toMessagesMessage()
        }
    }

    fun getMessage(userId: Int, friendId: Int, messageId: Int): MessagesMessage? {
        friendService.getFriendship(userId, friendId) ?: return null

        val message = messageService.getMessage(messageId) ?: return null

        if (message.author.id != userId && message.author.id == friendId) {
            return null
        }

        return message.toMessagesMessage()
    }

    fun sendMessage(userId: Int, friendId: Int, content: String): Boolean {
        val friends = friendService.getFriendship(userId, friendId) ?: return false

        return messageService.createMessage(userId, friends.channel.id, content) != null
    }

    fun deleteMessage(userId: Int, friendId: Int, messageId: Int): Boolean {
        friendService.getFriendship(userId, friendId) ?: return false

        val message = messageService.getMessage(messageId) ?: return false

        if (message.author.id != userId && message.author.id == friendId) {
            return false
        }

        return messageService.deleteMessage(messageId)
    }

    fun editMessage(userId: Int, friendId: Int, messageId: Int, content: String): Boolean {
        friendService.getFriendship(userId, friendId) ?: return false

        val message = messageService.getMessage(messageId) ?: return false

        if (message.author.id != userId && message.author.id == friendId) {
            return false
        }

        return messageService.editMessage(messageId, content)
    }
}