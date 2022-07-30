package database.servicies.guildChannels

import database.servicies.guilds.DatabaseGuild
import database.servicies.guilds.DatabaseGuilds
import database.servicies.guilds.Guild
import database.servicies.guilds.GuildChannelService
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text

interface GuildChannelOrdering {
    val guild: Guild
    val channels: String

    fun parse(channelService: GuildChannelService, guild: Int): Chans {
        return Chans.fromString(channels, channelService, guild)
    }
}

interface DatabaseGuildChannelOrdering : Entity<DatabaseGuildChannelOrdering>, GuildChannelOrdering {
    companion object : Entity.Factory<DatabaseGuildChannelOrdering>()

    override val guild: DatabaseGuild
    override val channels: String
}

object DatabaseGuildChannelOrderings : Table<DatabaseGuildChannelOrdering>("guild_channel_ordering") {
    val channels = text("channels").bindTo { it.channels }
    val guild_id = int("guild_id").primaryKey().references(DatabaseGuilds) { it.guild }
}