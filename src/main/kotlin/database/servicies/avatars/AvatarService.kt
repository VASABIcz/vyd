package database.servicies.avatars

interface AvatarService {
    suspend fun getAvatar(id: Int): ByteArray?
    suspend fun setAvatar(id: Int, avatar: ByteArray): Boolean
    suspend fun removeAvatar(id: Int): Boolean
}