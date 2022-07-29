package database.servicies.avatars

interface DefaultAvatarService {
    fun getAvatar(id: Int): ByteArray?

    fun createAvatar(avatar: ByteArray): Boolean

    fun setAvatar(id: Int, avatar: ByteArray): Boolean
}