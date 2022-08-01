package database.servicies.friendRequests

interface FriendRequestService {
    suspend fun createRequest(requester: Int, receiver: Int): Int?

    suspend fun getRequestState(requester: Int, receiver: Int): FriendRequest?

    suspend fun getRequestState(id: Int): FriendRequest?

    suspend fun changePendingRequestState(id: Int, state: FriendRequestState, receiver: Int): Boolean

    suspend fun getPendingRequests(receiver: Int): List<FriendRequest>
}