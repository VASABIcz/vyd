package database.servicies.friends

import websockets.DispatcherService

class EventFriendService(private val base: FriendService, private val dispatcher: DispatcherService) :
    FriendService by base {
    override suspend fun sendMessage(sender: Int, receiver: Int, content: String): Int? =
        base.sendMessage(sender, receiver, content)?.also {
            dispatcher.sendDM(receiver, sender, content)
        }
}