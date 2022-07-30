package data.responses

@kotlinx.serialization.Serializable
data class GuildsGuild(val name: String, val owner: UsersUser, val timestamp: Long, val id: Int)
