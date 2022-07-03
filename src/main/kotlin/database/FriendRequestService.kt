package database

interface FriendRequestService {
    fun createRequest(requester: DatabaseUser, receiver: DatabaseUser): Boolean

    fun getRequestState(requester: DatabaseUser, receiver: DatabaseUser): DatabaseFriendRequest?

    fun getRequestState(id: Int): DatabaseFriendRequest?

    // fun deletePendingRequest(requester: DatabaseUser, receiver: DatabaseUser): Boolean

    // fun changePendingRequestState(requester: DatabaseUser, receiver: DatabaseUser, state: FriendRequestState): Boolean

    fun changePendingRequestState(id: Int, state: FriendRequestState, receiver: DatabaseUser): Boolean

    fun getPendingRequests(receiver: DatabaseUser): List<DatabaseFriendRequest>
}