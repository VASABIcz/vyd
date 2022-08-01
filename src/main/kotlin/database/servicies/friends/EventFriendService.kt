package database.servicies.friends

import database.servicies.messages.Message
import ifLaunch
import websockets.DispatcherService

class EventFriendService(private val base: FriendService, private val dispatcher: DispatcherService) : FriendService {
    override suspend fun addFriend(user1: Int, user2: Int): Boolean = base.addFriend(user1, user2).ifLaunch {
        dispatcher.addFriend(user1, user2)
        dispatcher.addFriend(user2, user1)
    }

    override suspend fun removeFriend(user1: Int, user2: Int): Boolean = base.removeFriend(user1, user2).ifLaunch {
        dispatcher.removeFriend(user1, user2)
        dispatcher.removeFriend(user2, user1)
    }

    override suspend fun getFriendship(user1: Int, user2: Int): Friend? = base.getFriendship(user1, user2)

    override suspend fun getFriends(user: Int): Set<Friend> = base.getFriends(user)

    override suspend fun sendMessage(sender: Int, receiver: Int, content: String): Int? =
        base.sendMessage(sender, receiver, content)?.also {
            dispatcher.sendDM(receiver, sender, content)
        }

    override suspend fun getMessages(user1: Int, user2: Int, amount: Int, offset: Int): List<Message>? =
        base.getMessages(user1, user2, amount, offset)
}