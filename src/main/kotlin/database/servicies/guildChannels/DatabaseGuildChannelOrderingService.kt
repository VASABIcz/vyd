package database.servicies.guildChannels

import database.servicies.guilds.GuildChannelService
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

    override fun getChannels(guild: Int): Chans? {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return null
        return chs.parse(guildChannelService, guild)
    }

    override fun moveCategory(channel: Int, guild: Int, position: Int): Boolean {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.moveCategory(channel, position)
        return update(guild, parsed.toString())
    }

    override fun deleteCategory(channel: Int, guild: Int): Boolean {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.removeCategory(channel)
        return update(guild, parsed.toString())
    }

    override fun createCategory(channel: Int, guild: Int): Boolean {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.addCategory(channel, guildChannelService, guild)
        return update(guild, parsed.toString())
    }

    override fun createChannel(channel: Int, guild: Int, category: Int?): Boolean {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.addChannel(channel, guildChannelService, guild, category)
        return update(guild, parsed.toString())
    }

    override fun deleteChannel(channel: Int, guild: Int): Boolean {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.removeChannel(channel)
        return update(guild, parsed.toString())
    }

    override fun moveChannel(channel: Int, guild: Int, category: Int?, position: Int): Boolean {
        val chs = ordering.find {
            it.guild_id eq guild
        } ?: return false
        val parsed = chs.parse(guildChannelService, guild)
        parsed.moveChannel(channel, category, position)
        return update(guild, parsed.toString())
    }

    override fun update(guild: Int, channels: String): Boolean {
        return database.insertOrUpdate(DatabaseGuildChannelOrderings) {
            set(it.guild_id, guild)
            set(it.channels, channels)
            onConflict {
                set(it.channels, channels)
            }
        } != 0
    }

    override fun createRecord(guild: Int): Boolean {
        return update(guild, "")
    }


}