package database.servicies.guilds

import data.responses.GuildsChannel
import data.responses.GuildsGuild
import data.responses.MembersMember
import database.servicies.channels.DatabaseChannel
import database.servicies.channels.DatabaseChannels
import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant

interface DatabaseGuild : Entity<DatabaseGuild> {
    companion object : Entity.Factory<DatabaseGuild>()

    val id: Int
    var name: String
    val owner: DatabaseUser
    val timestamp: Instant

    fun toGuildsGuild(): GuildsGuild {
        return GuildsGuild(name, owner, timestamp.toEpochMilli(), id)
    }
}

object DatabaseGuilds : Table<DatabaseGuild>("guilds") {
    val id = int("id").primaryKey().bindTo { it.id }
    val owner_id = int("owner_id").references(DatabaseUsers) { it.owner }
    val name = varchar("name").bindTo { it.name }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
}

interface DatabaseMember : Entity<DatabaseMember> {
    companion object : Entity.Factory<DatabaseMember>()

    val user: DatabaseUser
    val guild: DatabaseGuild
    var nick: String
    val timestamp: Instant

    fun toGuildsGuild(): GuildsGuild {
        return guild.toGuildsGuild()
    }

    fun toMembersMember(): MembersMember {
        return MembersMember(user.toUsersUser(), nick, guild.toGuildsGuild(), timestamp.toEpochMilli())
    }
}

object DatabaseMembers : Table<DatabaseMember>("members") {
    val user_id = int("user_id").references(DatabaseUsers) { it.user }
    val guild_id = int("guild_id").references(DatabaseGuilds) { it.guild }
    val nick = varchar("nick").bindTo { it.nick }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
}

interface DatabaseGuildChannel : Entity<DatabaseGuildChannel> {
    companion object : Entity.Factory<DatabaseGuildChannel>()

    val guild: DatabaseGuild
    val channel: DatabaseChannel
    var name: String

    fun toGuildsChannel(): GuildsChannel {
        return GuildsChannel(guild.toGuildsGuild(), name, channel.toChannelsChannel())
    }
}

object DatabaseGuildChannels : Table<DatabaseGuildChannel>("guild_channels") {
    val channel_id = int("channel_id").references(DatabaseChannels) { it.channel }
    val guild_id = int("guild_id").references(DatabaseGuilds) { it.guild }
    val name = varchar("name").bindTo { it.name }
}