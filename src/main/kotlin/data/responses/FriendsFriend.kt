package data.responses

@kotlinx.serialization.Serializable
data class FriendsFriend(val id: UsersUser, val channel: Int, val timestamp: Long, val friends: Boolean)
