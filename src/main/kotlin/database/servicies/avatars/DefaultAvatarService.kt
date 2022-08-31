package database.servicies.avatars

interface DefaultAvatarService : AvatarService {
    fun createAvatar(avatar: ByteArray): Boolean
}