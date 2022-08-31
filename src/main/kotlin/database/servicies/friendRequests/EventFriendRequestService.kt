package database.servicies.friendRequests

import websockets.DispatcherService

class EventFriendRequestService(private val base: FriendRequestService, private val dispatcher: DispatcherService) :
    FriendRequestService by base {
    override suspend fun createRequest(requester: Int, receiver: Int): Int? =
        base.createRequest(requester, receiver)?.also {
            dispatcher.friendRequest(requester, receiver, it)
        }
}