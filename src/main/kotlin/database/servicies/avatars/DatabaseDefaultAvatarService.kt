package database.servicies.avatars

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.support.mysql.insertOrUpdate

class DatabaseDefaultAvatarService(private val database: Database) : DefaultAvatarService {
    private val avatars = database.sequenceOf(DatabaseDefaultAvatars)

    override suspend fun getAvatar(id: Int): ByteArray? {
        return avatars.find {
            it.id eq id
        }?.image
    }

    override fun createAvatar(avatar: ByteArray): Boolean {
        return database.insert(DatabaseDefaultAvatars) {
            set(it.image, avatar)
        } != 0
    }

    override suspend fun removeAvatar(id: Int): Boolean {
        val avatar = avatars.find {
            it.id eq id
        } ?: return false
        avatar.delete()
        return avatar.flushChanges() != 0
    }

    override suspend fun setAvatar(id: Int, avatar: ByteArray): Boolean {
        // FIXME should be update i think
        return database.insertOrUpdate(DatabaseDefaultAvatars) {
            set(it.image, avatar)
            set(it.id, id)
        } != 0
    }
}