package database.servicies.avatars

interface AvatarsService {
    suspend fun createGuildAvatar(id: Int, image: ByteArray): Boolean

    suspend fun createUserAvatar(id: Int, image: ByteArray): Boolean

    suspend fun getGuildAvatar(id: Int): ByteArray?

    suspend fun getUserAvatar(id: Int): ByteArray?
}