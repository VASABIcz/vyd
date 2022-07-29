package data.responses

import database.servicies.users.User

@kotlinx.serialization.Serializable
data class GuildsGuild(val name: String, val owner: User, val timestamp: Long, val id: Int)
