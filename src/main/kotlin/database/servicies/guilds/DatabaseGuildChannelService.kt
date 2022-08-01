package database.servicies.guilds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseGuildChannelService(private val database: Database) : GuildChannelService {
    val channels get() = database.sequenceOf(DatabaseGuildChannels)
    override suspend fun createChannel(guild: Int, channel: Int, name: String): Int? = withContext(Dispatchers.IO) {
        if (name.contains('(') || name.contains(')') || name.contains(',')) {
            return@withContext null
        }

        return@withContext database.insertAndGenerateKey(DatabaseGuildChannels) {
            set(it.guild_id, guild)
            set(it.channel_id, channel)
            set(it.name, name)
        } as Int?
    }

    override suspend fun editChannel(id: Int, guild: Int, name: String): Boolean = withContext(Dispatchers.IO) {
        val channel = getChannel(id, guild) ?: return@withContext false
        channel.name = name
        return@withContext channel.flushChanges() > 0
    }

    override suspend fun getChannels(guild: Int): List<DatabaseGuildChannel> = withContext(Dispatchers.IO) {
        return@withContext channels.filter {
            DatabaseGuildChannels.guild_id eq guild
        }.toList()
    }

    override suspend fun getChannels(guild: Int, vararg chans: Int): List<GuildChannel> = withContext(Dispatchers.IO) {
        return@withContext channels.filter {
            it.channel_id inList chans.toList()
        }.toList()
    }

    override suspend fun getChannel(id: Int, guild: Int): DatabaseGuildChannel? = withContext(Dispatchers.IO) {
        return@withContext channels.find {
            (it.channel_id eq id) and (it.guild_id eq guild)
        }
    }
}