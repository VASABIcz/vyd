package database.servicies.guilds

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseGuildChannelService(private val database: Database) : GuildChannelService {
    val channels get() = database.sequenceOf(DatabaseGuildChannels)
    override fun createChannel(guild: Int, channel: Int, name: String): Int? {
        return database.insertAndGenerateKey(DatabaseGuildChannels) {
            set(it.guild_id, guild)
            set(it.channel_id, channel)
            set(it.name, name)
        } as Int?
    }

    override fun editChannel(id: Int, guild: Int, name: String): Boolean {
        val channel = getChannel(id, guild) ?: return false
        channel.name = name
        return channel.flushChanges() > 0
    }

    override fun getChannels(guild: Int): List<DatabaseGuildChannel> {
        return channels.filter {
            DatabaseGuildChannels.guild_id eq guild
        }.toList()
    }

    override fun getChannel(id: Int, guild: Int): DatabaseGuildChannel? {
        return channels.find {
            (it.channel_id eq id) and (it.guild_id eq guild)
        }
    }
}