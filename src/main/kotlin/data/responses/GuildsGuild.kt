package data.responses

import database.servicies.users.DatabaseUser

@kotlinx.serialization.Serializable
data class GuildsGuild(val name: String, val owner: DatabaseUser, val timestamp: Long, val id: Int)
