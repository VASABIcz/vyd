package data.requests

@kotlinx.serialization.Serializable
data class SigninUserId(val id: Int, val password: String)
