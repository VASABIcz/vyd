package database.servicies.avatars

import database.servicies.dms.DatabaseDmAvatars
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class DatabaseDmAvatarService(private val database: Database) : AvatarService {
    val avatars = database.sequenceOf(DatabaseDmAvatars)

    override suspend fun getAvatar(dm: Int): ByteArray? {
        return avatars.find {
            DatabaseDmAvatars.id eq dm
        }?.avatar
    }

    override suspend fun setAvatar(dm: Int, avatar: ByteArray): Boolean {
        return database.insert(DatabaseDmAvatars) {
            set(DatabaseDmAvatars.id, dm)
            set(DatabaseDmAvatars.avatar, avatar)
        } != 0
    }

    override suspend fun removeAvatar(id: Int): Boolean {
        val avatar = avatars.find {
            it.id eq id
        } ?: return false
        avatar.delete()
        return avatar.flushChanges() != 0
    }
}