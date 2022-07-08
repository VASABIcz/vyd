package data.requests

import database.servicies.friendRequests.FriendRequestResponse

@kotlinx.serialization.Serializable
data class RespondFriendRequest(val id: Int, val response: FriendRequestResponse)
