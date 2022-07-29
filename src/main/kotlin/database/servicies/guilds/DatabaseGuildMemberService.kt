package database.servicies.guilds

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.*

class DatabaseGuildMemberService(private val database: Database) : GuildMemberService {
    private val members get() = database.sequenceOf(DatabaseMembers)

    override fun joinGuild(user: Int, guild: Int): Boolean {
        return database.insert(DatabaseMembers) {
            set(DatabaseMembers.user_id, user)
            set(DatabaseMembers.guild_id, guild)
        } > 0
    }

    override fun leaveGuild(user: Int, guild: Int): Boolean {
        return (getMember(user, guild)?.delete() ?: -1) > 0 // FIXME ?
    }

    override fun changeNick(user: Int, guild: Int, nick: String): Boolean {
        val member = getMember(user, guild) ?: return false

        member.nick = nick
        return member.flushChanges() > 0
    }

    override fun getMember(user: Int, guild: Int): DatabaseMember? {
        return members.find {
            (DatabaseMembers.user_id eq user) and (DatabaseMembers.guild_id eq guild)
        }
    }

    override fun getMembers(guild: Int, amount: Int, offset: Int): List<DatabaseMember> {
        return members.filter {
            DatabaseMembers.guild_id eq guild
        }.drop(offset).take(amount).toList()
    }

    override fun getGuilds(user: Int): List<DatabaseMember> {
        return members.filter {
            DatabaseMembers.user_id eq user
        }.toList()
    }
}