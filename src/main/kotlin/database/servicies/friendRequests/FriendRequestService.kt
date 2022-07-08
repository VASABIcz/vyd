package database.servicies.friendRequests

interface FriendRequestService {
    fun createRequest(requester: Int, receiver: Int): Boolean

    fun getRequestState(requester: Int, receiver: Int): DatabaseFriendRequest?

    fun getRequestState(id: Int): DatabaseFriendRequest?

    fun changePendingRequestState(id: Int, state: FriendRequestState, receiver: Int): Boolean

    fun getPendingRequests(receiver: Int): List<DatabaseFriendRequest>
}