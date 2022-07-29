package database.servicies.avatars

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class DatabaseAvatarsService(private val database: Database) : AvatarsService {
    // FIXME i should use coroutines + Dispatcher.IO
    private val guildAvatars = database.sequenceOf(DatabaseGuildImages)
    private val userAvatars = database.sequenceOf(DatabaseUsersImages)

    override fun createGuildAvatar(id: Int, image: ByteArray): Boolean {
        return database.insert(DatabaseGuildImages) {
            set(it.image, image)
            set(it.id, id)
        } > 0
    }

    override fun createUserAvatar(id: Int, image: ByteArray): Boolean {
        return database.insert(DatabaseUsersImages) {
            set(it.image, image)
            set(it.id, id)
        } > 0
    }

    override fun getGuildAvatar(id: Int): ByteArray? {
        return guildAvatars.find {
            it.id eq id
        }?.image
    }

    override fun getUserAvatar(id: Int): ByteArray? {
        return userAvatars.find {
            it.id eq id
        }?.image
    }
}