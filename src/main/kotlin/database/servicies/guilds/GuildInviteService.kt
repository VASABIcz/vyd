package database.servicies.guilds

import java.time.Instant

interface GuildInviteService {
    fun createInvite(guild: Int, user: Int, url: String, expire: Instant?, maxUses: Int?): String?

    fun getInvite(url: String): GuildInvite?

    fun getInvites(guild: Int): List<GuildInvite>

    fun removeInvite(url: String): Boolean

    fun incrementInvites(url: String): Boolean
}