package database.servicies.guilds

import data.responses.GuildsChannel
import data.responses.GuildsGuild
import data.responses.MembersMember
import database.servicies.channels.Channel
import database.servicies.channels.DatabaseChannel
import database.servicies.channels.DatabaseChannels
import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import database.servicies.users.User
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant

interface DatabaseGuild : Entity<DatabaseGuild>, Guild {
    companion object : Entity.Factory<DatabaseGuild>()

    override val id: Int
    override var name: String
    override val owner: DatabaseUser
    override val timestamp: Instant
}

interface Guild {
    val id: Int
    var name: String
    val owner: User
    val timestamp: Instant

    fun toGuildsGuild(): GuildsGuild {
        return GuildsGuild(name, owner.toUsersUser(), timestamp.toEpochMilli(), id)
    }
}

interface GuildMember {
    val user: User
    val guild: Guild
    var nick: String?
    val timestamp: Instant

    fun toGuildsGuild(): GuildsGuild {
        return guild.toGuildsGuild()
    }

    fun toMembersMember(): MembersMember {
        return MembersMember(user.toUsersUser(), nick, guild.toGuildsGuild(), timestamp.toEpochMilli())
    }
}

interface GuildChannel {
    val guild: Guild
    val channel: Channel
    var name: String

    fun toGuildsChannel(): GuildsChannel {
        return GuildsChannel(guild.toGuildsGuild(), name, channel.toChannelsChannel())
    }
}

object DatabaseGuilds : Table<DatabaseGuild>("guilds") {
    val id = int("id").primaryKey().bindTo { it.id }
    val owner_id = int("owner_id").references(DatabaseUsers) { it.owner }
    val name = varchar("name").bindTo { it.name }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
}

interface DatabaseMember : Entity<DatabaseMember>, GuildMember {
    companion object : Entity.Factory<DatabaseMember>()

    override val user: DatabaseUser
    override val guild: DatabaseGuild
    override var nick: String?
    override val timestamp: Instant
}

object DatabaseMembers : Table<DatabaseMember>("members") {
    val user_id = int("user_id").references(DatabaseUsers) { it.user }
    val guild_id = int("guild_id").references(DatabaseGuilds) { it.guild }
    val nick = varchar("nick").bindTo { it.nick }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
}

interface DatabaseGuildChannel : Entity<DatabaseGuildChannel>, GuildChannel {
    companion object : Entity.Factory<DatabaseGuildChannel>()

    override val guild: DatabaseGuild
    override val channel: DatabaseChannel
    override var name: String
}

object DatabaseGuildChannels : Table<DatabaseGuildChannel>("guilds_channels") {
    val channel_id = int("channel_id").primaryKey().references(DatabaseChannels) { it.channel }
    val guild_id = int("guild_id").references(DatabaseGuilds) { it.guild }
    val name = varchar("name").bindTo { it.name }
}