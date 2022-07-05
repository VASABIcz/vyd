package data.responses

@kotlinx.serialization.Serializable
data class FriendsFriend(val friend: UsersUser, val channel: Int, val timestamp: Long, val friends: Boolean)
