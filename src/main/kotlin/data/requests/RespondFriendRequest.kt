package data.requests

import database.FriendRequestResponse

@kotlinx.serialization.Serializable
data class RespondFriendRequest(val id: Int, val response: FriendRequestResponse)
