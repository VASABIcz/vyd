package data.responses

@kotlinx.serialization.Serializable
data class UsersUser(val id: Int, val name: String, val discriminator: String)
