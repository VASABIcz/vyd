package database.servicies.guilds

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import java.time.Instant

class DatabaseGuildInviteService(private val database: Database) : GuildInviteService {
    private val invites = database.sequenceOf(DatabaseGuildInvites)

    override fun createInvite(guild: Int, user: Int, url: String, expire: Instant?, maxUses: Int?): String? {
        return if (database.insert(DatabaseGuildInvites) {
                set(it.url, url)
                set(it.guild_id, guild)
                set(it.author, user)
                set(it.expire, expire)
                set(it.maxUses, maxUses)
            } == 0) {
            null
        } else {
            url
        }
    }

    override fun getInvite(url: String): GuildInvite? {
        return invites.find {
            it.url eq url
        }
    }

    override fun getInvites(guild: Int): List<GuildInvite> {
        return invites.filter {
            it.guild_id eq guild
        }.toList()
    }

    override fun removeInvite(url: String): Boolean {
        val inv = invites.find {
            it.url eq url
        } ?: return false
        return inv.delete() != 0
    }

    override fun incrementInvites(url: String): Boolean {
        val inv = invites.find {
            it.url eq url
        } ?: return false

        inv.uses += 1
        return inv.flushChanges() != 0
    }
}