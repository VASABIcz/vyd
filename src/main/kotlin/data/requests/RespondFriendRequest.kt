package data.requests

import database.FriendRequestState

@kotlinx.serialization.Serializable
data class RespondFriendRequest(val id: Int, val response: FriendRequestState)
