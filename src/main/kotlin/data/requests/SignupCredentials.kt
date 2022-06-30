package data.requests

@kotlinx.serialization.Serializable
data class SignupCredentials(val username: String, val password: String)