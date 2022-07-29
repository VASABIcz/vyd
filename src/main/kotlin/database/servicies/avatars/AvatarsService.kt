package database.servicies.avatars

interface AvatarsService {
    fun createGuildAvatar(id: Int, image: ByteArray): Boolean

    fun createUserAvatar(id: Int, image: ByteArray): Boolean

    fun getGuildAvatar(id: Int): ByteArray?

    fun getUserAvatar(id: Int): ByteArray?
}