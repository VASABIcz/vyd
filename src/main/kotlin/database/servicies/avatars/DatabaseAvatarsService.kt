package database.servicies.avatars

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class DatabaseAvatarsService(private val database: Database) : AvatarsService {
    // FIXME i should use coroutines + Dispatcher.IO
    private val guildAvatars = database.sequenceOf(DatabaseGuildImages)
    private val userAvatars = database.sequenceOf(DatabaseUsersImages)

    override suspend fun createGuildAvatar(id: Int, image: ByteArray): Boolean = withContext(Dispatchers.IO) {
        return@withContext database.insert(DatabaseGuildImages) {
            set(it.image, image)
            set(it.id, id)
        } > 0
    }

    override suspend fun createUserAvatar(id: Int, image: ByteArray): Boolean = withContext(Dispatchers.IO) {
        return@withContext database.insert(DatabaseUsersImages) {
            set(it.image, image)
            set(it.id, id)
        } > 0
    }

    override suspend fun getGuildAvatar(id: Int): ByteArray? = withContext(Dispatchers.IO) {
        return@withContext guildAvatars.find {
            it.id eq id
        }?.image
    }

    override suspend fun getUserAvatar(id: Int): ByteArray? = withContext(Dispatchers.IO) {
        return@withContext userAvatars.find {
            it.id eq id
        }?.image
    }
}