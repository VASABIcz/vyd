package data.requests

@kotlinx.serialization.Serializable
data class Credentials(val username: String, val password: String)