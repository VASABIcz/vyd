package database.servicies.guilds

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class DatabaseGuildService(private val database: Database) : GuildService {
    private val guilds get() = database.sequenceOf(DatabaseGuilds)

    override fun createGuild(owner: Int, name: String): Int? {
        return database.insertAndGenerateKey(DatabaseGuilds) {
            set(DatabaseGuilds.owner_id, owner)
            set(DatabaseGuilds.name, name)
        } as Int?
    }

    override fun deleteGuild(id: Int): Boolean {
        val guild = getGuild(id) ?: return false

        return guild.delete() > 0
    }

    override fun editGuild(id: Int, name: String): Boolean {
        val guild = getGuild(id) ?: return false

        guild.name = name
        return guild.flushChanges() > 0
    }

    override fun getGuild(id: Int): DatabaseGuild? {
        return guilds.find { DatabaseGuilds.id eq id }
    }

    override fun renameGuild(id: Int, name: String): Boolean {
        val guild = getGuild(id) ?: return false

        guild.name = name
        return guild.flushChanges() > 0
    }
}