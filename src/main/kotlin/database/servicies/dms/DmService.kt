package database.servicies.dms

interface DmService {
    suspend fun createGroup(owner: Int, name: String = "untiteled"): Int?
    suspend fun createFriend(): Int?
    suspend fun createDm(): Int?

    suspend fun deleteDm(id: Int): Boolean

    // category created commands
    suspend fun changeName(id: Int, name: String): Boolean
    suspend fun changeAvatar(id: Int, avatar: ByteArray): Boolean

    // Friend oriented commands
    suspend fun switchToFriend(dm: Int): Boolean
    suspend fun switchToDm(dm: Int): Boolean

    suspend fun getChannel(dm: Int): DmChannel?
    suspend fun getChannels(vararg dm: Int): List<DmChannel>
}