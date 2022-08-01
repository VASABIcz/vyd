package database.servicies.guildChannels

import database.servicies.guilds.GuildChannelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.support.postgresql.insertOrUpdate

class DatabaseGuildChannelOrderingService(
    private val database: Database,
    private val guildChannelService: GuildChannelService
) : GuildChannelOrderingService {
    private val ordering = database.sequenceOf(DatabaseGuildChannelOrderings)
    // this is more like wrapper than service

    override suspend fun getChannels(guild: Int): Chans? = withContext(Dispatchers.IO) {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return@withContext null
        return@withContext chs.parse(guildChannelService, guild)
    }

    override suspend fun moveCategory(
        channel: Int,
        guild: Int,
        position: Int,
        a: GuildChannelOrderingService
    ): Boolean = withContext(Dispatchers.IO) {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return@withContext false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.moveCategory(channel, position)
        return@withContext a.update(guild, parsed)
    }

    override suspend fun deleteCategory(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean =
        withContext(Dispatchers.IO) {
            val chs = ordering.find {
                it.guild_id eq guild
            } ?: return@withContext false
            val parsed = chs.parse(guildChannelService, guild)
            parsed.removeCategory(channel)
            return@withContext a.update(guild, parsed)
        }

    override suspend fun createCategory(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean =
        withContext(Dispatchers.IO) {
            val chs = ordering.find {
                it.guild_id eq guild
            } ?: return@withContext false
            val parsed = chs.parse(guildChannelService, guild)
            parsed.addCategory(channel, guildChannelService, guild)
            return@withContext a.update(guild, parsed)
        }

    override suspend fun createChannel(
        channel: Int,
        guild: Int,
        category: Int?,
        a: GuildChannelOrderingService
    ): Boolean = withContext(Dispatchers.IO) {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return@withContext false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.addChannel(channel, guildChannelService, guild, category)
        return@withContext a.update(guild, parsed)
    }

    override suspend fun deleteChannel(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean =
        withContext(Dispatchers.IO) {
            val chs = ordering.find {
                it.guild_id eq guild
            } ?: return@withContext false
            val parsed = chs.parse(guildChannelService, guild)
            parsed.removeChannel(channel)
            return@withContext a.update(guild, parsed)
        }

    override suspend fun moveChannel(
        channel: Int,
        guild: Int,
        category: Int?,
        position: Int,
        a: GuildChannelOrderingService
    ): Boolean = withContext(Dispatchers.IO) {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return@withContext false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.moveChannel(channel, category, position)
        return@withContext a.update(guild, parsed)
    }

    override suspend fun update(guild: Int, channels: Chans): Boolean = withContext(Dispatchers.IO) {
        val str = channels.toString()
        return@withContext database.insertOrUpdate(DatabaseGuildChannelOrderings) {
            set(it.guild_id, guild)
            set(it.channels, str)
            onConflict {
                set(it.channels, str)
            }
        } != 0
    }

    override suspend fun createRecord(guild: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext database.insertOrUpdate(DatabaseGuildChannelOrderings) {
            set(it.guild_id, guild)
            set(it.channels, "")
            onConflict {
                set(it.channels, "")
            }
        } != 0
    }
}