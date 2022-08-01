package database.servicies.friendRequests

import websockets.DispatcherService

class EventFriendRequestService(private val base: FriendRequestService, private val dispatcher: DispatcherService) :
    FriendRequestService {
    override suspend fun createRequest(requester: Int, receiver: Int): Int? =
        base.createRequest(requester, receiver)?.also {
            dispatcher.friendRequest(requester, receiver, it)
        }

    override suspend fun getRequestState(requester: Int, receiver: Int): FriendRequest? =
        base.getRequestState(requester, receiver)

    override suspend fun getRequestState(id: Int): FriendRequest? = base.getRequestState(id)

    override suspend fun changePendingRequestState(id: Int, state: FriendRequestState, receiver: Int): Boolean =
        base.changePendingRequestState(id, state, receiver)

    override suspend fun getPendingRequests(receiver: Int): List<FriendRequest> = base.getPendingRequests(receiver)
}