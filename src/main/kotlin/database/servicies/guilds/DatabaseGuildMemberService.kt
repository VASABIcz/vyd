package database.servicies.guilds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.*

class DatabaseGuildMemberService(private val database: Database) : GuildMemberService {
    private val members get() = database.sequenceOf(DatabaseMembers)

    override suspend fun joinGuild(user: Int, guild: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext database.insert(DatabaseMembers) {
            set(DatabaseMembers.user_id, user)
            set(DatabaseMembers.guild_id, guild)
        } > 0
    }

    override suspend fun leaveGuild(user: Int, guild: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext (getMember(user, guild)?.delete() ?: -1) > 0 // FIXME ?
    }

    override suspend fun changeNick(user: Int, guild: Int, nick: String): Boolean = withContext(Dispatchers.IO) {
        val member = getMember(user, guild) ?: return@withContext false

        member.nick = nick
        return@withContext member.flushChanges() > 0
    }

    override suspend fun getMember(user: Int, guild: Int): DatabaseMember? = withContext(Dispatchers.IO) {
        return@withContext members.find {
            (DatabaseMembers.user_id eq user) and (DatabaseMembers.guild_id eq guild)
        }
    }

    override suspend fun getMembers(guild: Int, amount: Int, offset: Int): List<DatabaseMember> =
        withContext(Dispatchers.IO) {
            return@withContext members.filter {
                DatabaseMembers.guild_id eq guild
            }.drop(offset).take(amount).toList()
        }

    override suspend fun getGuilds(user: Int): List<DatabaseMember> = withContext(Dispatchers.IO) {
        return@withContext members.filter {
            DatabaseMembers.user_id eq user
        }.toList()
    }
}