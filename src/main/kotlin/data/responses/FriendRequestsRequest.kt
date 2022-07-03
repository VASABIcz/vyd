package data.responses

@kotlinx.serialization.Serializable
data class FriendRequestsRequest(val id: Int, val requester: UsersUser, val timestamp: Long)
