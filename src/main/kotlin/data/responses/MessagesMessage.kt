package data.responses

@kotlinx.serialization.Serializable
data class MessagesMessage(
    val id: Int,
    val author: UsersUser,
    val content: String,
    val timestamp: Long,
    val channel_id: Int
)
