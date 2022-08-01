package wrapers

import Config
import data.responses.MessagesMessage
import database.servicies.friends.FriendService
import database.servicies.messages.MessageService
import websockets.DispatcherService

class FriendWrapper(
    private val friendService: FriendService,
    private val messageService: MessageService,
    private val dispatcher: DispatcherService
) {
    suspend fun getMessages(
        userId: Int,
        friendId: Int,
        amount: Int? = Config.messageAmountDefault,
        offset: Int? = 0
    ): List<MessagesMessage>? {
        val friends = friendService.getFriendship(userId, friendId) ?: return null

        var amount = amount ?: Config.messageAmountDefault
        val offset = offset ?: 0


        if (amount > Config.messageAmountLimit) {
            amount = Config.messageAmountDefault
        } else if (amount < 0) {
            return null
        }

        val messages = messageService.getMessages(friends.channel.id, amount, offset)

        return messages.map {
            it.toMessagesMessage()
        }
    }

    suspend fun getMessage(userId: Int, friendId: Int, messageId: Int): MessagesMessage? {
        friendService.getFriendship(userId, friendId) ?: return null

        val message = messageService.getMessage(messageId) ?: return null

        if (message.author.id != userId && message.author.id == friendId) {
            return null
        }

        return message.toMessagesMessage()
    }

    suspend fun sendMessage(userId: Int, friendId: Int, content: String): Boolean {
        val friends = friendService.getFriendship(userId, friendId) ?: return false

        return messageService.createMessage(userId, friends.channel.id, content)?.also {
            dispatcher.sendDM(userId, friendId, content)
        } != null
    }

    suspend fun deleteMessage(userId: Int, friendId: Int, messageId: Int): Boolean {
        friendService.getFriendship(userId, friendId) ?: return false

        val message = messageService.getMessage(messageId) ?: return false

        if (message.author.id != userId && message.author.id == friendId) {
            return false
        }

        return messageService.deleteMessage(messageId)
    }

    suspend fun editMessage(userId: Int, friendId: Int, messageId: Int, content: String): Boolean {
        friendService.getFriendship(userId, friendId) ?: return false

        val message = messageService.getMessage(messageId) ?: return false

        if (message.author.id != userId && message.author.id == friendId) {
            return false
        }

        return messageService.editMessage(messageId, content)
    }
}