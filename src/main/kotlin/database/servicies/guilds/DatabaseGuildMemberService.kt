package database.servicies.guilds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.database.asIterable
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.*

class DatabaseGuildMemberService(private val database: Database) : GuildMemberService {
    private val members get() = database.sequenceOf(DatabaseMembers)

    override suspend fun joinGuild(user: Int, guild: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext database.insert(DatabaseMembers) {
            set(it.user_id, user)
            set(it.guild_id, guild)
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
            (it.user_id eq user) and (it.guild_id eq guild)
        }
    }

    override suspend fun getMember(member: Int): DatabaseMember? = withContext(Dispatchers.IO) {
        return@withContext members.find {
            it.id eq member
        }
    }

    override suspend fun getMembers(guild: Int, amount: Int, offset: Int): List<DatabaseMember> =
        withContext(Dispatchers.IO) {
            return@withContext members.filter {
                it.guild_id eq guild
            }.drop(offset).take(amount).toList()
        }

    override suspend fun getGuilds(user: Int): List<DatabaseMember> = withContext(Dispatchers.IO) {
        println("getting guilds for $user")
        return@withContext members.filter {
            it.user_id eq user
        }.toList().also {
            println("result guilds $it")
        }
    }

    override suspend fun getGuilds(user: Int, user1: Int): List<Int> {
        val sql = """
            select m.guild_id from guild_members 
                join guild_members as m 
                    on guild_members.guild_id = m.guild_id 
                    and guild_members.user_id = ? 
                    and m.user_id = ?
        """.trimIndent()

        return database.useConnection { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(0, user)
                statement.setInt(1, user1)
                statement.executeQuery().asIterable().map {
                    it.getInt(1)
                }
            }
        }
    }
}