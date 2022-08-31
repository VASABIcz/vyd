package database.servicies.dms

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.dsl.less
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.time.Instant

class DatabaseDmInviteService(private val database: Database) : DmInviteService {
    private val invites = database.sequenceOf(DatabaseGroupInvites)

    override suspend fun getPendingInvite(member: Int, dm: Int): GroupInvite? {
        return invites.find {
            (it.id eq dm) and (it.author eq dm) and (it.timestamp less it.expire)
        }
    }

    override suspend fun getPendingInvite(url: String): GroupInvite? {
        return invites.find {
            (it.url eq url) and (it.timestamp less it.expire)
        }
    }

    override suspend fun createInvite(member: Int, dm: Int): String? {
        return database.insertAndGenerateKey(DatabaseGroupInvites) {
            set(it.author, member)
            set(it.id, dm)
            set(it.expire, Instant.now().plusSeconds(24 * 60 * 60))
        } as String?
    }
}