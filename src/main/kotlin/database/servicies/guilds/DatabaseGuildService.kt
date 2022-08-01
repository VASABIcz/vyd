package database.servicies.guilds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class DatabaseGuildService(private val database: Database) : GuildService {
    private val guilds get() = database.sequenceOf(DatabaseGuilds)

    override suspend fun createGuild(owner: Int, name: String): Int? = withContext(Dispatchers.IO) {
        return@withContext database.insertAndGenerateKey(DatabaseGuilds) {
            set(DatabaseGuilds.owner_id, owner)
            set(DatabaseGuilds.name, name)
        } as Int?
    }

    override suspend fun deleteGuild(id: Int): Boolean = withContext(Dispatchers.IO) {
        val guild = getGuild(id) ?: return@withContext false

        return@withContext guild.delete() > 0
    }

    override suspend fun editGuild(id: Int, name: String): Boolean = withContext(Dispatchers.IO) {
        val guild = getGuild(id) ?: return@withContext false

        guild.name = name
        return@withContext guild.flushChanges() > 0
    }

    override suspend fun getGuild(id: Int): DatabaseGuild? = withContext(Dispatchers.IO) {
        return@withContext guilds.find { DatabaseGuilds.id eq id }
    }

    override suspend fun renameGuild(id: Int, name: String): Boolean = withContext(Dispatchers.IO) {
        val guild = getGuild(id) ?: return@withContext false

        guild.name = name
        return@withContext guild.flushChanges() > 0
    }
}