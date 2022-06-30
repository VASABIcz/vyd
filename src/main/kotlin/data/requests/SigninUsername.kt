package data.requests

@kotlinx.serialization.Serializable
data class SigninUsername(val username: String, val discriminator: String, val password: String)
