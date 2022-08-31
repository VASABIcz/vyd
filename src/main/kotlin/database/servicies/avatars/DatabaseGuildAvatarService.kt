package database.servicies.avatars

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.support.mysql.insertOrUpdate
import java.time.Instant

class DatabaseGuildAvatarService(private val database: Database) : AvatarService {
    val avatars = database.sequenceOf(DatabaseGuildImages)
    override suspend fun getAvatar(id: Int): ByteArray? {
        return avatars.find {
            it.id eq id
        }?.image
    }

    override suspend fun setAvatar(id: Int, avatar: ByteArray): Boolean {
        return database.insertOrUpdate(DatabaseGuildImages) {
            set(it.image, avatar)
            onDuplicateKey {
                set(it.image, avatar)
                set(it.timestamp, Instant.now())
            }
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