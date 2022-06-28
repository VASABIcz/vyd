package database

interface UsernameService {
    fun getDiscriminator(username: String): String?
    fun incrementDiscriminator(username: String): Boolean
}